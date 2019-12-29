package cc.chenhe.lib.wearmsger.compatibility

import android.content.Context
import android.net.Uri
import android.os.Handler
import cc.chenhe.lib.wearmsger.DATA_ID_KEY
import cc.chenhe.lib.wearmsger.WM
import cc.chenhe.lib.wearmsger.bean.Result
import cc.chenhe.lib.wearmsger.bean.toCompat
import cc.chenhe.lib.wearmsger.compatibility.data.Asset
import cc.chenhe.lib.wearmsger.compatibility.data.DataItem
import cc.chenhe.lib.wearmsger.compatibility.data.DataMapItem
import cc.chenhe.lib.wearmsger.compatibility.data.PutDataMapRequest
import cc.chenhe.lib.wearmsger.listener.DataListener
import cc.chenhe.lib.wearmsger.listener.MessageListener
import cc.chenhe.lib.wearmsger.logw
import com.mobvoi.android.wearable.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * MMS (Ticwear) 系统的 API 实现。
 */
internal object MmsImpl : ClientCompat {
    private const val TAG = "MmsImpl"

    private val listeners: MutableMap<Any, Any> by lazy { mutableMapOf<Any, Any>() }

    /**
     * 由于 mobvoi sdk 的实时监听器在后台线程被调用，所以要转到主线程。
     */
    private var handler: Handler? = null

    private fun getHandler(context: Context): Handler {
        if (handler == null) {
            synchronized(this) {
                if (handler == null) {
                    handler = Handler(context.applicationContext.mainLooper)
                }
            }
        }
        return handler!!
    }

    override fun removeAllListeners(context: Context) {
        for ((k, v) in listeners) {
            if (k is MessageListener && v is MessageApi.MessageListener) {
                Wearable.MessageApi.removeListener(WM.mobvoiApiClient, v)
            } else if (k is DataListener && v is DataApi.DataListener) {
                Wearable.DataApi.removeListener(WM.mobvoiApiClient, v)
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
        try {
            val r = Wearable.MessageApi.sendMessage(WM.mobvoiApiClient, nodeId, path, data)
                .await(timeout, TimeUnit.MILLISECONDS)
            when {
                r.status.isSuccess -> Result(Result.RESULT_OK, r.requestId.toLong())
                r.status.isTimeout -> Result(Result.RESULT_TIMEOUT, 0)
                r.status.isInterrupted || r.status.isCanceled -> Result(
                    Result.RESULT_INTERRUPTED,
                    0
                )
                else -> Result(Result.RESULT_FAIL, 0)
            }
        } catch (e: Exception) {
            Result(Result.RESULT_FAIL, 0)
        }
    }

    override suspend fun getNodesId(context: Context): List<String>? = withContext(Dispatchers.IO) {
        try {
            Wearable.NodeApi.getConnectedNodes(WM.mobvoiApiClient).await().nodes.map { it.id }
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
        MessageApi.MessageListener { event ->
            if (match(uri, literal, event)) {
                getHandler(context).post {
                    listener.preProcess(event.toCompat())
                }
            }
        }.let {
            Wearable.MessageApi.addListener(WM.mobvoiApiClient, it)
            listeners[listener] = it
        }
    }

    private fun match(uri: Uri?, literal: Boolean, event: MessageEvent): Boolean {
        if (uri == null) return true
        if (uri.scheme != "wear") return false
        if (uri.host != null && uri.host != "*" && uri.host != event.sourceNodeId) return false
        return uri.path?.let {
            if (literal) {
                event.path == it
            } else {
                event.path.startsWith(it)
            }
        } ?: !literal
    }

    private fun match(uri: Uri?, literal: Boolean, uri2: Uri): Boolean {
        if (uri == null) return true
        if (uri.scheme != "wear") return false
        if (uri.host != null && uri.host != "*" && uri.host != uri2.host) return false
        return uri.path?.let {
            if (literal) {
                uri2.path == it
            } else {
                uri2.path!!.startsWith(it)
            }
        } ?: !literal
    }

    override fun removeMessageListener(context: Context, listener: MessageListener) {
        if (listeners.containsKey(listener)) {
            listeners[listener]?.let {
                if (it is MessageApi.MessageListener) {
                    Wearable.MessageApi.removeListener(WM.mobvoiApiClient, it)
                    listeners.remove(listener)
                }
            }
        }
    }

    override suspend fun putData(
        context: Context,
        putDataMapRequest: PutDataMapRequest,
        timeout: Long,
        withId: Boolean
    ): Result = withContext(Dispatchers.IO) {
        putDataMapRequest.mms?.let {
            var id = 0L
            if (withId) {
                id = System.nanoTime()
                it.dataMap.putLong(DATA_ID_KEY, id)
            }
            try {
                val r = Wearable.DataApi.putDataItem(WM.mobvoiApiClient, it.asPutDataRequest())
                    .await(timeout, TimeUnit.MILLISECONDS)
                when {
                    r.status.isSuccess -> Result(Result.RESULT_OK, id)
                    r.status.isTimeout -> Result(Result.RESULT_TIMEOUT)
                    r.status.isInterrupted || r.status.isCanceled -> Result(
                        Result.RESULT_INTERRUPTED
                    )
                    else -> Result(Result.RESULT_FAIL)
                }
            } catch (e: Exception) {
                Result(Result.RESULT_TIMEOUT)
            }
        } ?: throw IllegalArgumentException("MMS needed, given is GMS or null.")
    }

    override suspend fun deleteData(
        context: Context,
        uri: Uri,
        timeout: Long
    ): Result = withContext(Dispatchers.IO) {
        try {
            val r = Wearable.DataApi.deleteDataItems(WM.mobvoiApiClient, uri)
                .await(timeout, TimeUnit.MILLISECONDS)
            when {
                r.status.isSuccess -> Result(Result.RESULT_OK)
                r.status.isTimeout -> Result(Result.RESULT_TIMEOUT)
                r.status.isInterrupted || r.status.isCanceled -> Result(
                    Result.RESULT_INTERRUPTED
                )
                else -> Result(Result.RESULT_FAIL)
            }
        } catch (e: Exception) {
            Result(Result.RESULT_TIMEOUT)
        }
    }

    override fun addDataListener(
        context: Context,
        listener: DataListener,
        uri: Uri?,
        literal: Boolean
    ) {
        DataApi.DataListener { buffer ->
            for (event in buffer) {
                if (!match(uri, literal, event.dataItem.uri)) {
                    continue
                }
                when (event.type) {
                    DataEvent.TYPE_CHANGED -> getHandler(context).post {
                        listener.preProcessChanged(
                            DataMapItem.fromDataItem(DataItem(event.dataItem))
                        )
                    }
                    DataEvent.TYPE_DELETED -> getHandler(context).post {
                        listener.preProcessDeleted(
                            DataMapItem.fromDataItem(DataItem(event.dataItem)).getUri()
                        )
                    }
                }
            }
        }.let {
            Wearable.DataApi.addListener(WM.mobvoiApiClient, it)
            listeners[listener] = it
        }
    }

    override fun removeDataListener(context: Context, listener: DataListener) {
        if (listeners.containsKey(listener)) {
            listeners[listener]?.let {
                if (it is DataApi.DataListener) {
                    Wearable.DataApi.removeListener(WM.mobvoiApiClient, it)
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
            Wearable.DataApi.getFdForAsset(WM.mobvoiApiClient, asset.mms!!)
                .await(timeout, TimeUnit.MILLISECONDS).inputStream
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}