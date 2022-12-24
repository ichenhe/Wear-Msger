package me.chenhe.lib.wearmsger.demo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.PutDataMapRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.chenhe.lib.wearmsger.BothWayHub
import me.chenhe.lib.wearmsger.DataHub
import me.chenhe.lib.wearmsger.MessageHub
import java.io.ByteArrayOutputStream

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var ctx: Context
    private lateinit var et: TextView
    private lateinit var iv: ImageView
    private lateinit var btnSendPhoto: Button
    private lateinit var tvResponse: TextView
    private lateinit var tvDataResponse: TextView

    private var photo: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = this
        setContentView(R.layout.activity_main)
        et = findViewById(R.id.editText)
        findViewById<Button>(R.id.btnSendMsg).setOnClickListener(this)
        findViewById<Button>(R.id.btnPutData).setOnClickListener(this)
        findViewById<Button>(R.id.btnDelData).setOnClickListener(this)
        findViewById<Button>(R.id.btnTakePhoto).setOnClickListener(this)
        btnSendPhoto =
            findViewById<Button>(R.id.btnSendPhoto).apply { setOnClickListener(this@MainActivity) }
        findViewById<Button>(R.id.btnDelPhoto).setOnClickListener(this)
        iv = findViewById(R.id.imageView)
        findViewById<Button>(R.id.btnRequest).setOnClickListener(this)
        findViewById<Button>(R.id.btnRequestData).setOnClickListener(this)
        tvResponse = findViewById(R.id.tvResponse)
        tvDataResponse = findViewById(R.id.tvResponseData)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnSendMsg -> sendMessage()
            R.id.btnPutData -> putData()
            R.id.btnDelData -> delData()
            R.id.btnTakePhoto -> dispatchTakePictureIntent()
            R.id.btnSendPhoto -> sendPhoto()
            R.id.btnDelPhoto -> delPhoto()
            R.id.btnRequest -> request()
            R.id.btnRequestData -> requestForData()
        }
    }

    private fun sendMessage() {
        lifecycleScope.launch {
            val mr = MessageHub.sendMessage(
                ctx,
                "/msg/test",
                et.text.toString()
            )
            Log.i("SendMsg", mr.toString())
        }
    }


    private fun putData() {
        lifecycleScope.launch {
            PutDataMapRequest.create("/data/test").setUrgent().let { req ->
                req.dataMap.run {
                    putString("main", et.text.toString())
                }
                val r = DataHub.putData(ctx, req)
                Log.i("PutData", r.toString())
            }
        }
    }

    private fun delData() {
        lifecycleScope.launch {
            val r = DataHub.deleteData(ctx, "/data/test")
            Log.i("DelData", r.toString())
        }
    }

    class TakePhoneContract : ActivityResultContract<Unit, Bitmap?>() {
        override fun createIntent(context: Context, input: Unit): Intent {
            return Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Bitmap? {
            if (resultCode != RESULT_OK || intent == null)
                return null
            val data = intent.extras?.get("data")
            if (data == null || data !is Bitmap)
                return null
            return data
        }
    }

    private val takePhotoLauncher = registerForActivityResult(TakePhoneContract()) { bitmap ->
        iv.setImageBitmap(bitmap)
        photo = bitmap
        btnSendPhoto.isEnabled = bitmap != null
    }

    private fun dispatchTakePictureIntent() {
        takePhotoLauncher.launch(Unit)
    }

    private fun sendPhoto() {
        lifecycleScope.launch(Dispatchers.IO) {
            photo?.let {
                val os = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.PNG, 100, os)
                val asset = Asset.createFromBytes(os.toByteArray())
                os.close()

                PutDataMapRequest.create("/data/photo").setUrgent().let { req ->
                    req.dataMap.putAsset("photo", asset)
                    req.dataMap.putLong("time", System.currentTimeMillis())
                    val r = DataHub.putData(ctx, req)
                    Log.i("SendPhoto", r.toString())
                }
            }
        }
    }

    private fun delPhoto() {
        lifecycleScope.launch(Dispatchers.IO) {
            val r =
                DataHub.deleteData(ctx, "/data/photo")
            Log.i("DelPhoto", r.toString())
        }
    }

    private fun request() {
        tvResponse.text = "Waiting for response..."
        lifecycleScope.launch(Dispatchers.IO) {
            val r = BothWayHub.requestForMessage(
                ctx,
                null,
                "/msg/request",
                et.text.toString()
            )
            withContext(Dispatchers.Main) {
                if (r.isSuccess()) {
                    tvResponse.text = "receive: nodeId=${r.responseNodeId}\n${r.getStringData()}"
                } else {
                    tvResponse.text = "fail: ${r.result}"
                }
            }
        }
    }

    private fun requestForData() {
        tvDataResponse.text = "Waiting for response..."
        lifecycleScope.launch(Dispatchers.IO) {
            val r = BothWayHub.requestForData(ctx, null, "/msg/request_data", et.text.toString())
            withContext(Dispatchers.Main) {
                if (r.isSuccess()) {
                    tvDataResponse.text = "receive: ${r.dataMapItem!!.dataMap.getString("data")}"
                } else {
                    tvDataResponse.text = "fail: ${r.result}"
                }
            }
        }
    }
}
