package cc.chenhe.lib.wearmsger

import android.net.Uri
import android.os.Looper
import android.util.Log
import cc.chenhe.lib.wearmsger.bean.MessageEvent
import cc.chenhe.lib.wearmsger.compatibility.data.DataMapItem
import cc.chenhe.lib.wearmsger.service.WMListenerService

internal class ListenerService : WMListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        i("msg", messageEvent.toString() + "\n decode=" + messageEvent.getStringData())
    }

    override fun onDataChanged(dataMapItem: DataMapItem) {
        super.onDataChanged(dataMapItem)
        i("data changed", dataMapItem.getDataMap().getString("main") ?: "None")
    }

    override fun onDataDeleted(uri: Uri) {
        super.onDataDeleted(uri)
        i("data deleted", uri.toString())
    }

    fun i(tag: String, msg: String) {
        Log.i("Service", "[$tag] $msg; UIThread=${Looper.myLooper() == Looper.getMainLooper()}")
    }
}