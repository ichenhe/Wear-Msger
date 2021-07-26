package me.chenhe.lib.wearmsger.service

import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.annotation.WorkerThread
import androidx.lifecycle.LifecycleService
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent

/**
 * 整合 GMS ，继承此类可接收所有协议的事件。
 *
 * # `Manifest` 声明
 * 通常情况下需要在 `Manifest` 声明两种 Service
 *
 * ## 1. [WMListenerService] 的子类
 *
 * 自行创建一个类继承 [WMListenerService] 以便统一接收各种事件。必须添加一个 `action` 为
 * `me.chenhe.lib.wearmsger.EVENT_LISTENER` 的过滤器，例如：
 *
 * ```xml
 * <service android:name=".ListenerService">
 *   <intent-filter>
 *      <action android:name="me.chenhe.lib.wearmsger.EVENT_LISTENER" />
 *   </intent-filter>
 * </service>
 * ```
 *
 * ## 2. [GmsListenerAgentService][me.chenhe.lib.wearmsger.service.GmsListenerAgentService]
 *
 * 用于代理并转发 GMS 的事件接收，需要按照
 * [Google Docs](https://developer.android.com/training/wearables/data-layer/events.html)
 * 的要求根据实际情况填写过滤器。例如：
 *
 * ```xml
 * <service android:name="me.chenhe.lib.wearmsger.service.GmsListenerAgentService">
 *  <intent-filter>
 *      <action android:name="com.google.android.gms.wearable.MESSAGE_RECEIVED" />
 *      <data
 *          android:host="*"
 *          android:pathPrefix="/test"
 *          android:scheme="wear" />
 *  </intent-filter>
 * </service>
 * ```
 */
abstract class WMListenerService : LifecycleService() {

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    inner class LocalBinder : android.os.Binder() {
        fun getService(): WMListenerService = this@WMListenerService
    }

    @WorkerThread
    open fun onMessageReceived(messageEvent: MessageEvent) {
    }

    /**
     * @param id 标识请求，不存在默认为0.
     */
    @WorkerThread
    open fun onDataChanged(
        dataMapItem: DataMapItem,
        id: Long = 0
    ) {
    }

    @WorkerThread
    open fun onDataDeleted(
        uri: Uri
    ) {
    }
}