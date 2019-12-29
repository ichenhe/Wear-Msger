package cc.chenhe.lib.wearmsger.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.annotation.WorkerThread

/**
 * 整合 GMS 与 MMS，继承此类可接收所有协议的事件。
 *
 * # `Manifest` 声明
 * 通常情况下需要在 `Manifest` 声明三种 Service
 *
 * ## 1. [WMListenerService] 的子类
 *
 * 自行创建一个类继承 [WMListenerService] 以便统一接收各种事件。必须添加一个 `action` 为
 * `cc.chenhe.lib.wearmsger.EVENT_LISTENER` 的过滤器，例如：
 *
 * ```xml
 * <service android:name=".ListenerService">
 *   <intent-filter>
 *      <action android:name="cc.chenhe.lib.wearmsger.EVENT_LISTENER" />
 *   </intent-filter>
 * </service>
 * ```
 *
 * ## 2. [GmsListenerAgentService][cc.chenhe.lib.wearmsger.service.GmsListenerAgentService]
 *
 * 用于代理并转发 GMS 的事件接收，需要按照
 * [Google Docs](https://developer.android.com/training/wearables/data-layer/events.html)
 * 的要求根据实际情况填写过滤器。例如：
 *
 * ```xml
 * <service android:name="cc.chenhe.lib.wearmsger.service.GmsListenerAgentService">
 *  <intent-filter>
 *      <action android:name="com.google.android.gms.wearable.MESSAGE_RECEIVED" />
 *      <data
 *          android:host="*"
 *          android:pathPrefix="/test"
 *          android:scheme="wear" />
 *  </intent-filter>
 * </service>
 * ```
 *
 * ## 3. [MmsListenerAgentService][cc.chenhe.lib.wearmsger.service.MmsListenerAgentService]
 *
 * 类似地，用于代理并转发 MMS 的事件接收，若无需兼容 Ticwear 系统可以不注册。过滤器要求与 GMS 类似，但是要将
 * `com.google.android.gms.wearable` 前缀替换为 `com.mobvoi.android.wearable`. 例如：
 *
 * ```xml
 * <service android:name="cc.chenhe.lib.wearmsger.service.MmsListenerAgentService">
 *  <intent-filter>
 *      <action android:name="com.mobvoi.android.wearable.MESSAGE_RECEIVED" />
 *      <data
 *          android:host="*"
 *          android:pathPrefix="/test"
 *          android:scheme="wear" />
 *  </intent-filter>
 * </service>
 * ```
 */
abstract class WMListenerService : Service() {

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : android.os.Binder() {
        fun getService(): WMListenerService = this@WMListenerService
    }

    @WorkerThread
    open fun onMessageReceived(messageEvent: cc.chenhe.lib.wearmsger.bean.MessageEvent) {
    }

    /**
     * @param id 标识请求，不存在默认为0.
     */
    @WorkerThread
    open fun onDataChanged(
        dataMapItem: cc.chenhe.lib.wearmsger.compatibility.data.DataMapItem,
        id: Long = 0
    ) {
    }

    @WorkerThread
    open fun onDataDeleted(
        uri: Uri
    ) {
    }
}