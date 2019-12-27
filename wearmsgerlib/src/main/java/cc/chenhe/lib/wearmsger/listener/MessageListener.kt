package cc.chenhe.lib.wearmsger.listener

import cc.chenhe.lib.wearmsger.bean.MessageEvent

interface MessageListener {
    fun preProcess(event: MessageEvent) {
        onMessageReceived(event)
    }

    fun onMessageReceived(messageEvent: MessageEvent)
}