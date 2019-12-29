package cc.chenhe.lib.wearmsger.demo

import android.net.Uri
import android.os.Looper
import android.util.Log
import cc.chenhe.lib.wearmsger.BothWayHub
import cc.chenhe.lib.wearmsger.bean.MessageEvent
import cc.chenhe.lib.wearmsger.compatibility.data.DataMapItem
import cc.chenhe.lib.wearmsger.service.WMListenerService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class ListenerService : WMListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        i("msg", messageEvent.toString() + "\n decode=" + messageEvent.getStringData())

        if (messageEvent.path == "/msg/request") {
            // Response the both way request
            GlobalScope.launch {
                BothWayHub.response(
                    this@ListenerService,
                    messageEvent,
                    "Hello!"
                )
            }
        }
    }

    override fun onDataChanged(dataMapItem: DataMapItem, id: Long) {
        super.onDataChanged(dataMapItem, id)
        i("data changed", dataMapItem.getDataMap().getString("main") ?: "None")
    }

    override fun onDataDeleted(uri: Uri) {
        super.onDataDeleted(uri)
        i("data deleted", uri.toString())
    }

    private fun i(tag: String, msg: String) {
        Log.i("Service", "[$tag] $msg; UIThread=${Looper.myLooper() == Looper.getMainLooper()}")
    }
}