package me.chenhe.lib.wearmsger.demo

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.wearable.MessageEvent
import me.chenhe.lib.wearmsger.service.WMListenerService

class ListenerService : WMListenerService() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        i("msg", messageEvent.toString() + "\n decode=" + String(messageEvent.data))
        handler.post {
            Toast.makeText(this, String(messageEvent.data), Toast.LENGTH_SHORT).show()
        }
    }

    private fun i(tag: String, msg: String) {
        Log.i("Service", "[$tag] $msg; UIThread=${Looper.myLooper() == Looper.getMainLooper()}")
    }
}