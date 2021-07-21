package me.chenhe.lib.wearmsger.demo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.chenhe.lib.wearmsger.DataHub
import me.chenhe.lib.wearmsger.MessageHub
import me.chenhe.lib.wearmsger.listener.DataListener
import me.chenhe.lib.wearmsger.listener.MessageListener

@SuppressLint("SetTextI18n")
class MainActivity : WearableActivity() {

    private lateinit var tv: TextView
    private lateinit var iv: ImageView
    private lateinit var ctx: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ctx = this
        tv = findViewById(R.id.text)
        iv = findViewById(R.id.imageView)
    }

    private val msgListener = object : MessageListener {
        override fun onMessageReceived(messageEvent: MessageEvent) {
            messageEvent.run {
                tv.text = "[msg] from=$sourceNodeId, path=$path, data=${String(data)}"
            }
        }
    }

    private val dataListener = object : DataListener {
        override fun onDataChanged(dataMapItem: DataMapItem) {
            dataMapItem.run {
                tv.text =
                    "[data changed] path=${uri.path}, data=${dataMap.getString("main")}"
                if (uri.path == "/data/photo") {
                    GlobalScope.launch {
                        val bitmap = BitmapFactory.decodeStream(
                            DataHub.getInputStreamForAsset(
                                ctx,
                                dataMap.getAsset("photo")!!
                            )
                        )
                        withContext(Dispatchers.Main) {
                            iv.setImageBitmap(bitmap)
                        }
                    }
                }
            }
        }

        override fun onDataDeleted(uri: Uri) {
            tv.text = "[data deleted] from=${uri.host}, path=${uri.path}"
            if (uri.path == "/data/photo") {
                iv.setImageBitmap(null)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MessageHub.addMessageListener(
            this,
            msgListener,
            Uri.parse("wear:/msg/test")
        )
        DataHub.addDataListener(this, dataListener)
    }

    override fun onStop() {
        super.onStop()
        MessageHub.removeMessageListener(
            this,
            msgListener
        )
        DataHub.removeDataListener(this, dataListener)
    }
}
