package cc.chenhe.lib.wearmsger.service

import cc.chenhe.lib.wearmsger.bean.toCompat
import cc.chenhe.lib.wearmsger.compatibility.data.DataItem
import cc.chenhe.lib.wearmsger.compatibility.data.DataMapItem
import com.mobvoi.android.wearable.DataEvent
import com.mobvoi.android.wearable.DataEventBuffer
import com.mobvoi.android.wearable.MessageEvent
import com.mobvoi.android.wearable.WearableListenerService
import java.util.concurrent.CountDownLatch

/**
 * MMS 监听服务。
 *
 * 接收 MMS(Ticwear) 的事件并转发到 [WMListenerService].
 */
internal class MmsListenerAgentService : WearableListenerService(), ListenerAgentService {

    override val tag = "MmsAgentService"
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
        if (buffer == null) return
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