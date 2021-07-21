package me.chenhe.lib.wearmsger.compatibility

import android.content.Context
import android.net.Uri
import com.google.android.gms.wearable.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import me.chenhe.lib.wearmsger.DATA_ID_KEY
import me.chenhe.lib.wearmsger.bean.Result
import me.chenhe.lib.wearmsger.listener.DataListener
import me.chenhe.lib.wearmsger.listener.MessageListener
import me.chenhe.lib.wearmsger.logw
import java.io.InputStream

/**
 * GMS (Wear OS by Google) 系统的 API 实现。
 */
internal object GmsImpl : ClientCompat {
    private const val TAG = "GmsImpl"

    private val listeners: MutableMap<Any, Any> by lazy { mutableMapOf() }

    override fun removeAllListeners(context: Context) {
        if (listeners.isEmpty())
            return
        val d = Wearable.getDataClient(context)
        val m = Wearable.getMessageClient(context)
        for ((k, v) in listeners) {
            if (k is MessageListener && v is MessageClient.OnMessageReceivedListener) {
                m.removeListener(v)
            } else if (k is DataListener && v is DataClient.OnDataChangedListener) {
                d.removeListener(v)
            } else {
                logw(
                    TAG,
                    "Unknown listener type: ${k::class.java.simpleName}, upstream: ${v::class.java.simpleName}"
                )
            }
        }
        listeners.clear()
    }

    override suspend fun sendMessage(
        context: Context,
        nodeId: String,
        path: String,
        data: ByteArray,
        timeout: Long
    ): Result = try {
        Result.ok(Wearable.getMessageClient(context).sendMessage(nodeId, path, data).await())
    } catch (e: CancellationException) {
        Result.canceled(e)
    } catch (e: Exception) {
        Result.failed(e)
    }

    override suspend fun getNodesId(context: Context): List<String>? = withContext(Dispatchers.IO) {
        try {
            Wearable.getNodeClient(context).connectedNodes.await().map { it.id }
        } catch (e: Exception) {
            logw(TAG, "Failed to get connected nodes.", e)
            null
        }
    }

    override fun addMessageListener(
        context: Context,
        listener: MessageListener,
        uri: Uri?,
        literal: Boolean
    ) {
        val upstream = MessageClient.OnMessageReceivedListener { event ->
            listener.preProcess(event)
        }
        if (uri == null) {
            Wearable.getMessageClient(context).addListener(upstream)
        } else {
            val type = if (literal) MessageClient.FILTER_LITERAL else MessageClient.FILTER_PREFIX
            Wearable.getMessageClient(context).addListener(upstream, uri, type)
        }
        listeners[listener] = upstream
    }

    override fun removeMessageListener(context: Context, listener: MessageListener) {
        listeners[listener]?.also {
            if (it is MessageClient.OnMessageReceivedListener) {
                Wearable.getMessageClient(context).removeListener(it)
                listeners.remove(listener)
            }
        }
    }

    override suspend fun putData(
        context: Context,
        putDataMapRequest: PutDataMapRequest,
        timeout: Long,
        withId: Boolean
    ): Result = try {
        var id = 0L
        if (withId) {
            id = System.nanoTime()
            putDataMapRequest.dataMap.putLong(DATA_ID_KEY, id)
        }
        Wearable.getDataClient(context).putDataItem(putDataMapRequest.asPutDataRequest()).await()
        Result.ok(id)
    } catch (e: CancellationException) {
        Result.canceled(e)
    } catch (e: Exception) {
        Result.failed(e)
    }

    /**
     * 若成功，则 [Result.requestId] 表示已删除的项目个数。
     */
    override suspend fun deleteData(
        context: Context,
        uri: Uri,
        timeout: Long
    ): Result = try {
        val num = Wearable.getDataClient(context).deleteDataItems(uri).await()
        Result.ok(num)
    } catch (e: CancellationException) {
        Result.canceled(e)
    } catch (e: Exception) {
        Result.failed(e)
    }

    override fun addDataListener(
        context: Context,
        listener: DataListener,
        uri: Uri?,
        literal: Boolean
    ) {
        val upstream = DataClient.OnDataChangedListener { buffer ->
            for (event in buffer) {
                when (event.type) {
                    DataEvent.TYPE_CHANGED ->
                        listener.preProcessChanged(DataMapItem.fromDataItem(event.dataItem))
                    DataEvent.TYPE_DELETED ->
                        listener.preProcessDeleted(DataMapItem.fromDataItem(event.dataItem).uri)
                }
            }
        }
        if (uri == null) {
            Wearable.getDataClient(context).addListener(upstream)
        } else {
            val type = if (literal) MessageClient.FILTER_LITERAL else MessageClient.FILTER_PREFIX
            Wearable.getDataClient(context).addListener(upstream, uri, type)
        }
        listeners[listener] = upstream
    }

    override fun removeDataListener(context: Context, listener: DataListener) {
        listeners[listener]?.also {
            if (it is DataClient.OnDataChangedListener) {
                Wearable.getDataClient(context).removeListener(it)
                listeners.remove(listener)
            }
        }
    }

    override suspend fun getInputStreamForAsset(
        context: Context,
        asset: Asset,
        timeout: Long
    ): InputStream? = withContext(Dispatchers.IO) {
        try {
            Wearable.getDataClient(context).getFdForAsset(asset).await().inputStream
        } catch (e: Exception) {
            logw(TAG, "Failed to getFdFroAsset.", e)
            null
        }
    }
}