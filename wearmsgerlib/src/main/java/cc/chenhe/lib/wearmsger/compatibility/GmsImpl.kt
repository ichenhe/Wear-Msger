package cc.chenhe.lib.wearmsger.compatibility

import android.content.Context
import android.net.Uri
import cc.chenhe.lib.wearmsger.DATA_ID_KEY
import cc.chenhe.lib.wearmsger.bean.Result
import cc.chenhe.lib.wearmsger.bean.toCompat
import cc.chenhe.lib.wearmsger.compatibility.data.Asset
import cc.chenhe.lib.wearmsger.compatibility.data.DataItem
import cc.chenhe.lib.wearmsger.compatibility.data.DataMapItem
import cc.chenhe.lib.wearmsger.listener.DataListener
import cc.chenhe.lib.wearmsger.listener.MessageListener
import cc.chenhe.lib.wearmsger.logw
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * GMS (Wear OS by Google) 系统的 API 实现。
 */
internal object GmsImpl : ClientCompat {
    private const val TAG = "GmsImpl"

    private val listeners: MutableMap<Any, Any> by lazy { mutableMapOf<Any, Any>() }

    override fun removeAllListeners(context: Context) {
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
    ): Result = withContext(Dispatchers.IO) {
        val sendMessageTask = Wearable.getMessageClient(context).sendMessage(nodeId, path, data)
        try {
            Result(
                Result.RESULT_OK,
                Tasks.await(sendMessageTask, timeout, TimeUnit.MILLISECONDS).toLong()
            )
        } catch (e: ExecutionException) {
            Result(Result.RESULT_FAIL, 0)
        } catch (e: InterruptedException) {
            Result(Result.RESULT_INTERRUPTED, 0)
        } catch (e: TimeoutException) {
            Result(Result.RESULT_TIMEOUT, 0)
        }
    }

    override suspend fun getNodesId(context: Context): List<String>? = withContext(Dispatchers.IO) {
        val nodeListTask = Wearable.getNodeClient(context).connectedNodes
        try {
            Tasks.await(nodeListTask).map { it.id }
        } catch (e: Exception) {
            null
        }
    }

    override fun addMessageListener(
        context: Context,
        listener: MessageListener,
        uri: Uri?,
        literal: Boolean
    ) {
        MessageClient.OnMessageReceivedListener { event ->
            listener.preProcess(event.toCompat())
        }.let {
            if (uri == null) {
                Wearable.getMessageClient(context).addListener(it)
            } else {
                Wearable.getMessageClient(context)
                    .addListener(
                        it,
                        uri,
                        if (literal) MessageClient.FILTER_LITERAL else MessageClient.FILTER_PREFIX
                    )
            }
            listeners[listener] = it
        }
    }

    override fun removeMessageListener(context: Context, listener: MessageListener) {
        if (listeners.containsKey(listener)) {
            listeners[listener]?.let {
                if (it is MessageClient.OnMessageReceivedListener) {
                    Wearable.getMessageClient(context).removeListener(it)
                    listeners.remove(listener)
                }
            }
        }
    }

    override suspend fun putData(
        context: Context,
        putDataMapRequest: cc.chenhe.lib.wearmsger.compatibility.data.PutDataMapRequest,
        timeout: Long,
        withId: Boolean
    ): Result = withContext(Dispatchers.IO) {
        putDataMapRequest.gms?.let {
            var id = 0L
            if (withId) {
                id = System.nanoTime()
                it.dataMap.putLong(DATA_ID_KEY, id)
            }
            val task = Wearable.getDataClient(context).putDataItem(it.asPutDataRequest())
            try {
                Tasks.await(task, timeout, TimeUnit.MILLISECONDS)
                Result(Result.RESULT_OK, id)
            } catch (e: ExecutionException) {
                Result(Result.RESULT_FAIL)
            } catch (e: InterruptedException) {
                Result(Result.RESULT_INTERRUPTED)
            } catch (e: TimeoutException) {
                Result(Result.RESULT_TIMEOUT)
            }
        } ?: throw IllegalArgumentException("GMS needed, given is MMS or null.")
    }

    override suspend fun deleteData(
        context: Context,
        uri: Uri,
        timeout: Long
    ): Result = withContext(Dispatchers.IO) {
        val task = Wearable.getDataClient(context).deleteDataItems(uri)
        try {
            Tasks.await(task, timeout, TimeUnit.MILLISECONDS)
            Result(Result.RESULT_OK)
        } catch (e: ExecutionException) {
            Result(Result.RESULT_FAIL)
        } catch (e: InterruptedException) {
            Result(Result.RESULT_INTERRUPTED)
        } catch (e: TimeoutException) {
            Result(Result.RESULT_TIMEOUT)
        }
    }

    override fun addDataListener(
        context: Context,
        listener: DataListener,
        uri: Uri?,
        literal: Boolean
    ) {
        DataClient.OnDataChangedListener { buffer ->
            for (event in buffer) {
                when (event.type) {
                    DataEvent.TYPE_CHANGED -> listener.preProcessChanged(
                        DataMapItem.fromDataItem(DataItem(event.dataItem))
                    )
                    DataEvent.TYPE_DELETED -> listener.preProcessDeleted(
                        DataMapItem.fromDataItem(DataItem(event.dataItem)).getUri()
                    )
                }
            }
        }.let {
            if (uri == null) {
                Wearable.getDataClient(context).addListener(it)
            } else {
                Wearable.getDataClient(context).addListener(
                    it,
                    uri,
                    if (literal) DataClient.FILTER_LITERAL else DataClient.FILTER_PREFIX
                )
            }
            listeners[listener] = it
        }
    }

    override fun removeDataListener(context: Context, listener: DataListener) {
        if (listeners.containsKey(listener)) {
            listeners[listener]?.let {
                if (it is DataClient.OnDataChangedListener) {
                    Wearable.getDataClient(context).removeListener(it)
                    listeners.remove(listener)
                }
            }
        }
    }

    override suspend fun getInputStreamForAsset(
        context: Context,
        asset: Asset,
        timeout: Long
    ): InputStream? = withContext(Dispatchers.IO) {
        try {
            Tasks.await(
                Wearable.getDataClient(context).getFdForAsset(asset.gms!!),
                timeout,
                TimeUnit.MILLISECONDS
            ).inputStream
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}