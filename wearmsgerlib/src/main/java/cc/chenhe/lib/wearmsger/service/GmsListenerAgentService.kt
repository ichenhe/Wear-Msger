package cc.chenhe.lib.wearmsger.service

import cc.chenhe.lib.wearmsger.bean.toCompat
import cc.chenhe.lib.wearmsger.compatibility.data.DataItem
import cc.chenhe.lib.wearmsger.compatibility.data.DataMapItem
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
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
    private val connection = BindListenerServiceConnection(this)

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

    override fun onMessageReceived(messageevent: MessageEvent?) {
        super.onMessageReceived(messageevent)
        delegateOnMessageReceived(messageevent?.toCompat())
    }

    override fun onDataChanged(buffer: DataEventBuffer?) {
        super.onDataChanged(buffer)
        if (buffer == null) {
            return
        }
        for (event in buffer) {
            when (event.type) {
                DataEvent.TYPE_CHANGED -> delegateOnDataChanged(
                    DataMapItem.fromDataItem(DataItem(event.dataItem))
                )
                DataEvent.TYPE_DELETED -> delegateOnDataDeleted(
                    DataMapItem.fromDataItem(DataItem(event.dataItem))
                )
            }
        }
    }

}