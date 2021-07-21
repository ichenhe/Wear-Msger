package me.chenhe.lib.wearmsger

import android.content.Context
import android.net.Uri
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.PutDataMapRequest
import me.chenhe.lib.wearmsger.bean.Result
import me.chenhe.lib.wearmsger.listener.DataListener
import java.io.InputStream

object DataHub {

    /**
     * 添加或修改一个 data 项。
     * 注意，若 path 和 data 内容没有变更，那么多次 put 不会重复触发监听。
     *
     * @param putDataMapRequest 通过 [PutDataMapRequest.create] 创建，并使用 [PutDataMapRequest.getDataMap] 来添加数据项。
     * @param withId 是否自动加入一个 id 字段用于标识请求。若加入 id 则视为 data 内容变更，不会被系统缓存。
     */
    suspend fun putData(
        context: Context,
        putDataMapRequest: PutDataMapRequest,
        timeout: Long = SEND_TIMEOUT,
        withId: Boolean = false
    ): Result {
        return getClient().putData(context, putDataMapRequest, timeout, withId)
    }

    suspend fun deleteData(
        context: Context,
        uri: Uri,
        timeout: Long = SEND_TIMEOUT
    ): Result {
        return getClient().deleteData(context, uri, timeout)
    }

    suspend fun deleteData(
        context: Context,
        path: String,
        timeout: Long = SEND_TIMEOUT
    ): Result {
        return getClient().deleteData(context, Uri.parse("wear:$path"), timeout)
    }

    fun addDataListener(
        context: Context,
        listener: DataListener,
        uri: Uri? = null,
        literal: Boolean = false
    ) {
        getClient().addDataListener(context, listener, uri, literal)
    }

    fun removeDataListener(context: Context, listener: DataListener) {
        getClient().removeDataListener(context, listener)
    }

    suspend fun getInputStreamForAsset(
        context: Context,
        asset: Asset,
        timeout: Long = SEND_TIMEOUT
    ): InputStream? {
        return getClient().getInputStreamForAsset(context, asset, timeout)
    }
}