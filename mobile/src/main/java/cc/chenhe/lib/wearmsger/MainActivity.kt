package cc.chenhe.lib.wearmsger

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
import androidx.appcompat.app.AppCompatActivity
import cc.chenhe.lib.wearmsger.compatibility.data.Asset
import cc.chenhe.lib.wearmsger.compatibility.data.PutDataMapRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 0
    }

    private lateinit var ctx: Context
    private lateinit var et: TextView
    private lateinit var iv: ImageView
    private lateinit var btnSendPhoto: Button

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
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnSendMsg -> sendMessage()
            R.id.btnPutData -> putData()
            R.id.btnDelData -> delData()
            R.id.btnTakePhoto -> dispatchTakePictureIntent()
            R.id.btnSendPhoto -> sendPhoto()
            R.id.btnDelPhoto -> delPhoto()
        }
    }

    private fun sendMessage() {
        GlobalScope.launch {
            val mr = MessageHub.sendMessage(ctx, "/msg/test", et.text.toString())
            Log.i("SendMsg", mr.toString())
        }
    }


    private fun putData() {
        GlobalScope.launch {
            PutDataMapRequest.create("/data/test").setUrgent().let { req ->
                req.getDataMap().run {
                    putString("main", et.text.toString())
                }
                val r = DataHub.putData(ctx, req)
                Log.i("PutData", r.toString())
            }
        }
    }

    private fun delData() {
        GlobalScope.launch {
            val r = DataHub.deleteData(ctx, "/data/test")
            Log.i("DelData", r.toString())
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            data?.extras?.get("data")?.let {
                if (it is Bitmap) {
                    iv.setImageBitmap(it)
                    photo = it
                    btnSendPhoto.isEnabled = true
                }
            }
        }
    }

    private fun sendPhoto() {
        GlobalScope.launch(Dispatchers.IO) {
            photo?.let {
                val os = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.PNG, 100, os)
                val asset = Asset.createFromBytes(os.toByteArray())
                os.close()

                PutDataMapRequest.create("/data/photo").setUrgent().let { req ->
                    req.getDataMap().putAsset("photo", asset)
                    req.getDataMap().putLong("time", System.currentTimeMillis())
                    val r = DataHub.putData(ctx, req)
                    Log.i("SendPhoto", r.toString())
                }
            }
        }
    }

    private fun delPhoto() {
        GlobalScope.launch(Dispatchers.IO) {
            val r = DataHub.deleteData(ctx, "/data/photo")
            Log.i("DelPhoto", r.toString())
        }
    }
}
