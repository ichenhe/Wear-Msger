package cc.chenhe.lib.wearmsger

import android.content.Context
import android.net.Uri
import cc.chenhe.lib.wearmsger.BothWayHub.response
import cc.chenhe.lib.wearmsger.bean.*
import cc.chenhe.lib.wearmsger.compatibility.data.DataMapItem
import cc.chenhe.lib.wearmsger.compatibility.data.PutDataMapRequest
import cc.chenhe.lib.wearmsger.listener.DataListener
import cc.chenhe.lib.wearmsger.listener.MessageListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.nio.charset.Charset
import kotlin.coroutines.resume

/**
 * 用于双向通讯的请求与响应。
 *
 * 双向通讯的响应 path 会加上 [WMRE] 以及 `{reqId}` 前缀，故响应数据原则上只能通过 request 的 [BothWayCallback]
 * 得到。而不建议通过 Service 获取,亦无法通过普通的 [MessageListener] 或 [DataListener] 监听。
 *
 * 对请求的响应必须调用此类下面的 [response] 方法，使用普通的 [MessageHub.sendMessage] 或 [DataHub.putData]
 * 不会被识别。
 */
object BothWayHub {

    //----------------------------------------------------------------------------------- msg->msg

    /**
     * 发送一个 message 请求并期望得到一个 message 响应。
     *
     * 若有多个节点响应或得到多次响应，则只返回第一个。
     *
     * @param nodeId 目标节点。`null` 则发送至所有节点。
     * @param data 要发送的数据。
     */
    @Suppress("unused")
    suspend fun requestForMessage(
        context: Context,
        nodeId: String?,
        path: String,
        data: ByteArray,
        timeout: Long = WM.bothWayTimeout
    ): MessageCallback {
        var listener: MessageListener? = null
        val r = withTimeoutOrNull(timeout) {
            val send = withContext(Dispatchers.IO) {
                if (nodeId == null) {
                    MessageHub.sendMessage(context.applicationContext, path, data, timeout)
                } else {
                    MessageHub.sendMessage(context.applicationContext, nodeId, path, data, timeout)
                }
            }
            if (!send.isSuccess()) {
                return@withTimeoutOrNull MessageCallback(BothWayCallback.Result.REQUEST_FAIL)
            } else {
                return@withTimeoutOrNull suspendCancellableCoroutine<MessageCallback> { cont ->
                    listener = object : MessageListener {
                        override fun preProcess(event: MessageEvent) {
                            super.preProcess(event)
                            cont.resume(
                                MessageCallback(
                                    BothWayCallback.Result.OK,
                                    event.sourceNodeId,
                                    event.data
                                )
                            )
                        }

                        override fun onMessageReceived(messageEvent: MessageEvent) {}
                    }
                    MessageHub.addMessageListener(
                        context.applicationContext,
                        listener!!,
                        Uri.parse("wear://*$WMRE/${send.requestId}$path")
                    )
                }
            }
        }
        listener?.let { MessageHub.removeMessageListener(context.applicationContext, it) }
        return if (listener == null) {
            MessageCallback(BothWayCallback.Result.REQUEST_FAIL)
        } else {
            r ?: MessageCallback(BothWayCallback.Result.TIMEOUT)
        }
    }

    /**
     * 发送一个 message 请求并期望得到一个 message 响应。
     *
     * 若有多个节点响应或得到多次响应，则只返回第一个。
     *
     * @param nodeId 目标节点。`null` 则发送至所有节点。
     * @param data 要发送的数据。
     */
    @Suppress("unused")
    suspend fun requestForMessage(
        context: Context,
        nodeId: String?,
        path: String,
        data: String,
        timeout: Long = WM.bothWayTimeout
    ): MessageCallback =
        requestForMessage(context, nodeId, path, data.toByteArray(Charset.forName("utf8")), timeout)

    /**
     * 以 message 形式响应一个 message 请求。
     *
     * @param request 要响应的请求。
     */
    @Suppress("unused")
    suspend fun response(
        context: Context,
        request: MessageEvent,
        data: ByteArray,
        timeout: Long = SEND_TIMEOUT
    ): Result = MessageHub.sendMessage(
        context,
        request.sourceNodeId,
        "$WMRE/${request.requestId}${request.path}",
        data,
        timeout
    )

    /**
     * 以 message 形式响应一个 message 请求。
     *
     * @param request 要响应的请求。
     */
    @Suppress("unused")
    suspend fun response(
        context: Context,
        request: MessageEvent,
        data: String,
        timeout: Long = SEND_TIMEOUT
    ): Result = response(context, request, data.toByteArray(Charset.forName("utf8")), timeout)

    //---------------------------------------------------------------------------------- msg->data

    /**
     * 发送一个 message 请求并期望得到一个 data 响应。
     *
     * 若有多个节点响应或得到多次响应，则只返回第一个。
     *
     * @param nodeId 目标节点。`null` 则发送至所有节点。
     * @param data 要发送的数据。
     */
    @Suppress("unused")
    suspend fun requestForData(
        context: Context,
        nodeId: String?,
        path: String,
        data: ByteArray,
        timeout: Long = WM.bothWayTimeout
    ): DataCallback {
        var listener: DataListener? = null
        val r = withTimeoutOrNull(timeout) {
            val send = withContext(Dispatchers.IO) {
                if (nodeId == null) {
                    MessageHub.sendMessage(context.applicationContext, path, data, timeout)
                } else {
                    MessageHub.sendMessage(context.applicationContext, nodeId, path, data, timeout)
                }
            }
            if (!send.isSuccess()) {
                return@withTimeoutOrNull DataCallback(BothWayCallback.Result.REQUEST_FAIL)
            } else {
                return@withTimeoutOrNull suspendCancellableCoroutine<DataCallback> { cont ->
                    listener = object : DataListener {
                        override fun preProcessChanged(dataMapItem: DataMapItem) {
                            super.preProcessChanged(dataMapItem)
                            cont.resume(
                                DataCallback(
                                    BothWayCallback.Result.OK,
                                    dataMapItem.getUri().host,
                                    dataMapItem
                                )
                            )
                        }

                        override fun preProcessDeleted(uri: Uri) {
                            super.preProcessDeleted(uri)
                            // 实际上这种情况不可能发生。因为响应数据会自动添加 path 前缀。而一个没有事先 put 的
                            // data 是不会响应 delete 请求的。
                            cont.resume(DataCallback(BothWayCallback.Result.OK, uri.host))
                        }

                        override fun onDataChanged(dataMapItem: DataMapItem) {}
                        override fun onDataDeleted(uri: Uri) {}
                    }
                    DataHub.addDataListener(
                        context.applicationContext,
                        listener!!,
                        Uri.parse("wear://*$WMRE/${send.requestId}$path")
                    )
                }
            }
        }
        listener?.let { DataHub.removeDataListener(context.applicationContext, it) }
        return if (listener == null) {
            DataCallback(BothWayCallback.Result.REQUEST_FAIL)
        } else {
            r ?: DataCallback(BothWayCallback.Result.TIMEOUT)
        }
    }

    /**
     * 发送一个 message 请求并期望得到一个 data 响应。
     *
     * 若有多个节点响应或得到多次响应，则只返回第一个。
     *
     * @param nodeId 目标节点。`null` 则发送至所有节点。
     * @param data 要发送的数据，以 utf8 编码。
     */
    @Suppress("unused")
    suspend fun requestForData(
        context: Context,
        nodeId: String?,
        path: String,
        data: String,
        timeout: Long = WM.bothWayTimeout
    ): DataCallback =
        requestForData(context, nodeId, path, data.toByteArray(Charset.forName("utf8")), timeout)

    /**
     * 获取一个 [PutDataMapRequest] 用于响应请求。
     *
     * @param request 要响应的 message 请求。
     */
    @Suppress("unused")
    fun obtainResponseDataRequest(request: MessageEvent) =
        PutDataMapRequest.create("$WMRE/${request.requestId}${request.path}")

    /**
     * 以 putData 形式响应一个请求。
     *
     * 为了确保能够及时到达目标设备，会自动设置 [PutDataMapRequest.setUrgent] 并附加一个唯一 id.
     *
     * @param putDataMapRequest 必须通过 [obtainResponseDataRequest] 函数获取。
     * @throws IllegalArgumentException 无法识别响应目标时抛出此异常。
     */
    @Suppress("unused")
    suspend fun response(
        context: Context,
        putDataMapRequest: PutDataMapRequest,
        timeout: Long = SEND_TIMEOUT
    ): Result {
        if (putDataMapRequest.getUri().path?.startsWith("$WMRE/") != true) {
            throw IllegalArgumentException("Could not identify response target. Make sure ues obtainResponseDataRequest() to get parameter.")
        }
        putDataMapRequest.setUrgent()
        return DataHub.putData(context, putDataMapRequest, timeout, true)
    }

    //----------------------------------------------------------------------------------- data->msg

    /**
     * 发送一个 putData 请求并期望得到一个 message 响应。
     *
     * 若有多个节点响应或得到多次响应，则只返回第一个。
     *
     * 为了确保请求及时到达，会自动设置 [PutDataMapRequest.setUrgent] 并附加一个唯一 id.
     */
    @Suppress("unused")
    suspend fun requestForMessage(
        context: Context,
        putDataMapRequest: PutDataMapRequest,
        timeout: Long = WM.bothWayTimeout
    ): MessageCallback {
        var listener: MessageListener? = null
        val r = withTimeoutOrNull(timeout) {
            val send = withContext(Dispatchers.IO) {
                putDataMapRequest.setUrgent()
                DataHub.putData(context, putDataMapRequest, timeout, true)
            }
            if (!send.isSuccess()) {
                return@withTimeoutOrNull MessageCallback(BothWayCallback.Result.REQUEST_FAIL)
            } else {
                return@withTimeoutOrNull suspendCancellableCoroutine<MessageCallback> { cont ->
                    listener = object : MessageListener {
                        override fun preProcess(event: MessageEvent) {
                            super.preProcess(event)
                            cont.resume(
                                MessageCallback(
                                    BothWayCallback.Result.OK,
                                    event.sourceNodeId,
                                    event.data
                                )
                            )
                        }

                        override fun onMessageReceived(messageEvent: MessageEvent) {}
                    }
                    MessageHub.addMessageListener(
                        context.applicationContext,
                        listener!!,
                        Uri.parse("wear://*$WMRE/${send.requestId}${putDataMapRequest.getUri().path}")
                    )
                }
            }
        }
        listener?.let { MessageHub.removeMessageListener(context.applicationContext, it) }
        return if (listener == null) {
            MessageCallback(BothWayCallback.Result.REQUEST_FAIL)
        } else {
            r ?: MessageCallback(BothWayCallback.Result.TIMEOUT)
        }
    }

    /**
     * 以 message 形式响应一个 putData 请求。
     *
     * @param request 要响应的请求。
     * @throws IllegalArgumentException 无法识别响应目标时抛出此异常。
     */
    @Suppress("unused")
    suspend fun response(
        context: Context,
        request: DataMapItem,
        data: ByteArray,
        timeout: Long = SEND_TIMEOUT
    ): Result {
        val id = request.getDataMap().getLong(DATA_ID_KEY, 0)
        if (id == 0L) {
            throw IllegalArgumentException("Could not identify response target.")
        }
        return request.getUri().run {
            host?.let {
                MessageHub.sendMessage(context, it, "$WMRE/$id$path", data, timeout)
            } ?: MessageHub.sendMessage(context, "$WMRE/$id$path", data, timeout)
        }
    }

    /**
     * 以 message 形式响应一个 putData 请求。
     *
     * @param request 要响应的请求。
     * @throws IllegalArgumentException 无法识别响应目标时抛出此异常。
     */
    @Suppress("unused")
    suspend fun response(
        context: Context,
        request: DataMapItem,
        data: String,
        timeout: Long = SEND_TIMEOUT
    ): Result = response(context, request, data.toByteArray(Charset.forName("utf8")), timeout)

}