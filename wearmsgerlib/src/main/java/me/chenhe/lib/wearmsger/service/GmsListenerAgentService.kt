package me.chenhe.lib.wearmsger.service

import com.google.android.gms.wearable.*
import java.util.concurrent.CountDownLatch

/**
 * GMS 监听服务。
 *
 * 接收 GMS(Wear OS) 的事件并转发到 [WMListenerService].
 */
internal class GmsListenerAgentService : WearableListenerService(), ListenerAgentService {

    override val tag = "GmsAgentService"
    override var targetService: WMListenerService? = null
    override val countDownLatch = CountDownLatch(1)
    private val connection = AgentServiceConnection(this)

    override fun onCreate() {
        super.onCreate()
        bindTargetService(this, connection)
    }

    override fun onDestroy() {
        try {
            unbindService(connection)
        } catch (e: Exception) {
        }
        super.onDestroy()
    }

    override fun onMessageReceived(messageevent: MessageEvent) {
        delegateOnMessageReceived(messageevent)
    }

    override fun onDataChanged(buffer: DataEventBuffer) {
        for (event in buffer) {
            when (event.type) {
                DataEvent.TYPE_CHANGED -> delegateOnDataChanged(
                    DataMapItem.fromDataItem(event.dataItem)
                )
                DataEvent.TYPE_DELETED -> delegateOnDataDeleted(
                    DataMapItem.fromDataItem(event.dataItem)
                )
            }
        }
    }
}