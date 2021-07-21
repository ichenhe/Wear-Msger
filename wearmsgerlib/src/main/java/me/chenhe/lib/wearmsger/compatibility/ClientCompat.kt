package me.chenhe.lib.wearmsger.compatibility

import android.content.Context
import android.net.Uri
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.PutDataMapRequest
import me.chenhe.lib.wearmsger.bean.Result
import me.chenhe.lib.wearmsger.listener.DataListener
import me.chenhe.lib.wearmsger.listener.MessageListener
import java.io.InputStream

/**
 * 定义了手表通信相关的 API，后端根据不同的系统交给不同的实现。
 */
internal interface ClientCompat {

    @Suppress("unused")
    fun removeAllListeners(context: Context)

    ///////////////////////////////////////////////////////////////////////////////////// Message

    @Suppress("unused")
    suspend fun sendMessage(
        context: Context,
        nodeId: String,
        path: String,
        data: String,
        timeout: Long
    ): Result {
        return sendMessage(context, nodeId, path, data.toByteArray(), timeout)
    }

    suspend fun sendMessage(
        context: Context,
        nodeId: String,
        path: String,
        data: ByteArray,
        timeout: Long
    ): Result

    /**
     * @return 获取失败返回 null.
     */
    suspend fun getNodesId(context: Context): List<String>?

    fun addMessageListener(
        context: Context,
        listener: MessageListener,
        uri: Uri? = null,
        literal: Boolean = false
    )

    fun removeMessageListener(context: Context, listener: MessageListener)

    ///////////////////////////////////////////////////////////////////////////////////// data

    suspend fun putData(
        context: Context,
        putDataMapRequest: PutDataMapRequest,
        timeout: Long,
        withId: Boolean
    ): Result

    suspend fun deleteData(
        context: Context,
        uri: Uri,
        timeout: Long
    ): Result

    fun addDataListener(
        context: Context,
        listener: DataListener,
        uri: Uri? = null,
        literal: Boolean = false
    )

    fun removeDataListener(context: Context, listener: DataListener)

    suspend fun getInputStreamForAsset(
        context: Context,
        asset: Asset,
        timeout: Long
    ): InputStream?
}