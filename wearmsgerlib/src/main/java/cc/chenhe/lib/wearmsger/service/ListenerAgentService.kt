package cc.chenhe.lib.wearmsger.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import cc.chenhe.lib.wearmsger.DATA_ID_KEY
import cc.chenhe.lib.wearmsger.bean.MessageEvent
import cc.chenhe.lib.wearmsger.compatibility.data.DataMapItem
import cc.chenhe.lib.wearmsger.logd
import cc.chenhe.lib.wearmsger.loge
import cc.chenhe.lib.wearmsger.logw
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 定义了转发原生事件到统一的监听服务所需的函数，并提供了默认实现。
 *
 * 默认情况下转发的函数保持原生线程不变，即后台线程，详情参阅
 * [Google doc](https://developer.android.com/training/wearables/data-layer/events.html#with-a-wearablelistenerservice).
 */
internal interface ListenerAgentService {
    val tag: String
    var targetService: WMListenerService?
    /**
     * 用于等待绑定到目标服务，初始值应该为1。
     */
    val countDownLatch: CountDownLatch

    /**
     * 尝试拉起并绑定到实际监听的服务。应在 [Service.onCreate] 中调用。
     */
    fun bindTargetService(context: Service, conn: BindListenerServiceConnection) {
        if (targetService != null) {
            return
        }
        try {
            if (context.bindService(
                    Intent("cc.chenhe.lib.wearmsger.EVENT_LISTENER").setPackage(context.packageName),
                    conn,
                    Context.BIND_AUTO_CREATE
                )
            ) {
                logd(tag, "Bind to WMListener service success.")
            } else {
                loge(tag, "Bind to WMListener service failed.")
            }
        } catch (e: Exception) {
            loge(tag, "Bind to WMListener service failed.")
            e.printStackTrace()
        }
    }

    private fun isServiceReady(): Boolean {
        return try {
            countDownLatch.await(100L, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            false
        }
    }

    fun delegateOnMessageReceived(messageEvent: MessageEvent?) {
        messageEvent?.run {
            try {
                if (isServiceReady()) {
                    targetService?.onMessageReceived(messageEvent)
                } else {
                    logDiscard("onMessageReceived")
                }
            } catch (e: RemoteException) {
                logRemoteE("onMessageReceived", e)
            }
        }
    }

    fun delegateOnDataChanged(dataMapItem: DataMapItem) {
        try {
            if (isServiceReady()) {
                if (dataMapItem.getDataMap().containsKey(DATA_ID_KEY)) {
                    targetService?.onDataChanged(
                        dataMapItem, dataMapItem.getDataMap().getLong(DATA_ID_KEY, 0)
                    )
                } else {
                    targetService?.onDataChanged(dataMapItem)
                }
            } else {
                logDiscard("onDataChanged")
            }
        } catch (e: RemoteException) {
            logRemoteE("onDataChanged", e)
        }
    }

    fun delegateOnDataDeleted(dataMapItem: DataMapItem) {
        try {
            if (isServiceReady()) {
                targetService?.onDataDeleted(dataMapItem.getUri())
            } else {
                logDiscard("onDataDeleted")
            }
        } catch (e: RemoteException) {
            logRemoteE("onDataDeleted", e)
        }
    }

    fun logDiscard(eventName: String) {
        logw(
            tag,
            "Discard a $eventName event, no WM listener service is ready."
        )
    }

    fun logRemoteE(eventName: String, e: RemoteException) {
        loge(
            tag,
            "Catch a remote exception in $eventName event."
        )
        e.printStackTrace()
    }
}

/**
 * 连接到目标 Service 并取得实例。
 */
internal class BindListenerServiceConnection(
    private val service: ListenerAgentService
) : ServiceConnection {
    override fun onServiceDisconnected(className: ComponentName?) {
        service.targetService = null
    }

    override fun onServiceConnected(className: ComponentName?, ibinder: IBinder?) {
        if (ibinder is WMListenerService.LocalBinder) {
            service.targetService = ibinder.getService()
            service.countDownLatch.countDown()
        } else {
            service.targetService = null
        }
    }

}