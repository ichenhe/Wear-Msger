package cc.chenhe.lib.wearmsger

import android.content.Context
import android.net.Uri
import cc.chenhe.lib.wearmsger.bean.DataResult
import cc.chenhe.lib.wearmsger.compatibility.data.Asset
import cc.chenhe.lib.wearmsger.compatibility.data.PutDataMapRequest
import cc.chenhe.lib.wearmsger.listener.DataListener
import java.io.InputStream

object DataHub {

    @Suppress("unused")
    suspend fun putData(
        context: Context,
        putDataMapRequest: PutDataMapRequest,
        timeout: Long = SEND_TIMEOUT
    ): DataResult {
        return getClient().putData(context, putDataMapRequest, timeout)
    }

    @Suppress("unused")
    suspend fun deleteData(
        context: Context,
        uri: Uri,
        timeout: Long = SEND_TIMEOUT
    ): DataResult {
        return getClient().deleteData(context, uri, timeout)
    }

    @Suppress("unused")
    suspend fun deleteData(
        context: Context,
        path: String,
        timeout: Long = SEND_TIMEOUT
    ): DataResult {
        return getClient().deleteData(context, Uri.parse("wear:$path"), timeout)
    }

    @Suppress("unused")
    fun addDataListener(
        context: Context,
        listener: DataListener,
        uri: Uri? = null,
        literal: Boolean = false
    ) {
        getClient().addDataListener(context, listener, uri, literal)
    }

    @Suppress("unused")
    fun removeDataListener(context: Context, listener: DataListener) {
        getClient().removeDataListener(context, listener)
    }

    @Suppress("unused")
    suspend fun getInputStreamForAsset(
        context: Context,
        asset: Asset,
        timeout: Long = SEND_TIMEOUT
    ): InputStream? {
        return getClient().getInputStreamForAsset(context, asset, timeout)
    }
}