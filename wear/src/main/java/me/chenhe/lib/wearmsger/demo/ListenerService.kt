package me.chenhe.lib.wearmsger.demo

import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import kotlinx.coroutines.launch
import me.chenhe.lib.wearmsger.BothWayHub
import me.chenhe.lib.wearmsger.service.WMListenerService

internal class ListenerService : WMListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        i("msg", messageEvent.toString() + "\n decode=" + String(messageEvent.data))
        if (messageEvent.path == "/msg/request") {
            // Response the both way request
            lifecycleScope.launch {
                BothWayHub.response(
                    this@ListenerService,
                    messageEvent,
                    "Hello!"
                )
            }
        } else if (messageEvent.path == "/msg/request_data") {
            lifecycleScope.launch {
                val resp = BothWayHub.obtainResponseDataRequest(messageEvent)
                resp.dataMap.putString("data", "Hello data!")
                BothWayHub.response(this@ListenerService, resp)
            }
        }
    }

    override fun onDataChanged(dataMapItem: DataMapItem, id: Long) {
        super.onDataChanged(dataMapItem, id)
        i("data changed", dataMapItem.dataMap.getString("main") ?: "None")
    }

    override fun onDataDeleted(uri: Uri) {
        super.onDataDeleted(uri)
        i("data deleted", uri.toString())
    }

    private fun i(tag: String, msg: String) {
        Log.i("Service", "[$tag] $msg; UIThread=${Looper.myLooper() == Looper.getMainLooper()}")
    }
}