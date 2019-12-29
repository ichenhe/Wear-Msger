package cc.chenhe.lib.wearmsger.listener

import androidx.annotation.MainThread
import cc.chenhe.lib.wearmsger.WMRE
import cc.chenhe.lib.wearmsger.bean.MessageEvent

interface MessageListener {
    @MainThread
    fun preProcess(event: MessageEvent) {
        // 过滤掉 BothWay 的响应，其应当交给对应的 Callback 处理。
        if (!event.path.startsWith("$WMRE/"))
            onMessageReceived(event)
    }

    @MainThread
    fun onMessageReceived(messageEvent: MessageEvent)
}