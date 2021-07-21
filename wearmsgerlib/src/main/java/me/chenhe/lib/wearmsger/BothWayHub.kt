package me.chenhe.lib.wearmsger

import android.content.Context
import android.net.Uri
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import me.chenhe.lib.wearmsger.BothWayHub.response
import me.chenhe.lib.wearmsger.bean.BothWayCallback
import me.chenhe.lib.wearmsger.bean.DataCallback
import me.chenhe.lib.wearmsger.bean.MessageCallback
import me.chenhe.lib.wearmsger.bean.Result
import me.chenhe.lib.wearmsger.listener.DataListener
import me.chenhe.lib.wearmsger.listener.MessageListener
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
    suspend fun requestForMessage(
        context: Context,
        nodeId: String?,
        path: String,
        data: ByteArray,
        timeout: Long = WM.bothWayTimeout
    ): MessageCallback {
        val r = withTimeoutOrNull(timeout) {
            val send = if (nodeId == null) {
                MessageHub.sendMessage(context.applicationContext, path, data, timeout)
            } else {
                MessageHub.sendMessage(context.applicationContext, nodeId, path, data, timeout)
            }

            if (!send.isSuccess) {
                return@withTimeoutOrNull MessageCallback(BothWayCallback.Result.REQUEST_FAIL)
            }
            suspendCancellableCoroutine { cont ->
                val listener = object : MessageListener {
                    override fun preProcess(event: MessageEvent) {
                        MessageHub.removeMessageListener(context.applicationContext, this)
                        cont.resume(MessageCallback.ok(event.sourceNodeId, event.data))
                    }

                    override fun onMessageReceived(messageEvent: MessageEvent) {}
                }
                // '*' is necessary because the data response will be added with some strange characters
                val filter = Uri.parse("wear://*$WMRE/${send.requestId}$path")
                MessageHub.addMessageListener(context.applicationContext, listener, filter)

                cont.invokeOnCancellation {
                    MessageHub.removeMessageListener(context.applicationContext, listener)
                }
            }
        }
        return r ?: MessageCallback(BothWayCallback.Result.TIMEOUT)
    }

    /**
     * 发送一个 message 请求并期望得到一个 message 响应。
     *
     * 若有多个节点响应或得到多次响应，则只返回第一个。
     *
     * @param nodeId 目标节点。`null` 则发送至所有节点。
     * @param data 要发送的数据。
     */
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
    suspend fun requestForData(
        context: Context,
        nodeId: String?,
        path: String,
        data: ByteArray,
        timeout: Long = WM.bothWayTimeout
    ): DataCallback {
        val r = withTimeoutOrNull(timeout) {
            val send = if (nodeId == null) {
                MessageHub.sendMessage(context.applicationContext, path, data, timeout)
            } else {
                MessageHub.sendMessage(context.applicationContext, nodeId, path, data, timeout)
            }

            if (!send.isSuccess) {
                return@withTimeoutOrNull DataCallback(BothWayCallback.Result.REQUEST_FAIL)
            }
            suspendCancellableCoroutine { cont ->
                val listener = object : DataListener {
                    override fun preProcessChanged(dataMapItem: DataMapItem) {
                        DataHub.removeDataListener(context.applicationContext, this)
                        cont.resume(DataCallback.ok(dataMapItem.uri.host, dataMapItem))
                    }

                    override fun preProcessDeleted(uri: Uri) {
                        // 实际上这种情况不可能发生。因为响应数据会自动添加 path 前缀。而一个没有事先 put 的
                        // data 是不会响应 delete 请求的。
                        DataHub.removeDataListener(context.applicationContext, this)
                        cont.resume(DataCallback(BothWayCallback.Result.OK, uri.host))
                    }

                    override fun onDataChanged(dataMapItem: DataMapItem) {}
                    override fun onDataDeleted(uri: Uri) {}
                }
                val filter = Uri.parse("wear://*$WMRE/${send.requestId}$path")
                DataHub.addDataListener(context.applicationContext, listener, filter)

                cont.invokeOnCancellation {
                    DataHub.removeDataListener(context.applicationContext, listener)
                }
            }
        }
        return r ?: DataCallback(BothWayCallback.Result.TIMEOUT)
    }

    /**
     * 发送一个 message 请求并期望得到一个 data 响应。
     *
     * 若有多个节点响应或得到多次响应，则只返回第一个。
     *
     * @param nodeId 目标节点。`null` 则发送至所有节点。
     * @param data 要发送的数据，以 utf8 编码。
     */
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
    suspend fun response(
        context: Context,
        putDataMapRequest: PutDataMapRequest,
        timeout: Long = SEND_TIMEOUT
    ): Result {
        if (putDataMapRequest.uri.path?.startsWith("$WMRE/") != true) {
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
    suspend fun requestForMessage(
        context: Context,
        putDataMapRequest: PutDataMapRequest,
        timeout: Long = WM.bothWayTimeout
    ): MessageCallback {
        val r = withTimeoutOrNull(timeout) {
            putDataMapRequest.setUrgent()
            val send = DataHub.putData(context, putDataMapRequest, timeout, true)
            if (!send.isSuccess) {
                return@withTimeoutOrNull MessageCallback(BothWayCallback.Result.REQUEST_FAIL)
            }
            suspendCancellableCoroutine { cont ->
                val listener = object : MessageListener {
                    override fun preProcess(event: MessageEvent) {
                        MessageHub.removeMessageListener(context.applicationContext, this)
                        cont.resume(MessageCallback.ok(event.sourceNodeId, event.data))
                    }

                    override fun onMessageReceived(messageEvent: MessageEvent) {}
                }
                val filter =
                    Uri.parse("wear://*$WMRE/${send.requestId}${putDataMapRequest.uri.path}")
                MessageHub.addMessageListener(context.applicationContext, listener, filter)

                cont.invokeOnCancellation {
                    MessageHub.removeMessageListener(context.applicationContext, listener)
                }
            }
        }

        return r ?: MessageCallback(BothWayCallback.Result.TIMEOUT)
    }

    /**
     * 以 message 形式响应一个 putData 请求。
     *
     * @param request 要响应的请求。
     * @throws IllegalArgumentException 无法识别响应目标时抛出此异常。
     */
    suspend fun response(
        context: Context,
        request: DataMapItem,
        data: ByteArray,
        timeout: Long = SEND_TIMEOUT
    ): Result {
        val id = request.dataMap.getLong(DATA_ID_KEY, 0)
        if (id == 0L) {
            throw IllegalArgumentException("Could not identify response target.")
        }
        return request.uri.run {
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
    suspend fun response(
        context: Context,
        request: DataMapItem,
        data: String,
        timeout: Long = SEND_TIMEOUT
    ): Result = response(context, request, data.toByteArray(Charset.forName("utf8")), timeout)

}