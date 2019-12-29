package cc.chenhe.lib.wearmsger

import android.content.Context
import android.os.Bundle
import com.mobvoi.android.common.api.MobvoiApiClient
import com.mobvoi.android.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal object MmsClientManager {
    private const val TAG = "MmsClientManager"

    @Volatile
    private var client: MobvoiApiClient? = null

    fun destroy() {
        client?.let {
            if (it.isConnected) {
                it.disconnect()
            }
            client = null
        }
        connCallback = null
        countDownLatch = null
        failListener = null
    }

    /**
     * 获取 [MobvoiApiClient] 实例。正常情况下返回即已连接。
     */
    suspend fun getClient(context: Context): MobvoiApiClient {
        createClient(context)
        // 这实际上是双重检查锁模式
        // 确保同时只尝试连接一次，其他请求等待连接结构
        if (checkConnect(client)) {
            return client!!
        }
        connect()
        return client!!
    }

    private fun createClient(context: Context) {
        if (client == null) {
            synchronized(this) {
                if (client == null) {
                    client = MobvoiApiClient.Builder(context.applicationContext)
                        .addApi(Wearable.API)
                        .build()
                }
            }
        }
    }

    private var connCallback: MobvoiApiClient.ConnectionCallbacks? = null
    private var failListener: MobvoiApiClient.OnConnectionFailedListener? = null
    private var countDownLatch: CountDownLatch? = null

    @Synchronized
    private suspend fun connect(): Boolean {
        val c = client ?: return false
        if (checkConnect(c)) {
            return true
        }
        return suspendCoroutine { cort ->
            connCallback = object : MobvoiApiClient.ConnectionCallbacks {
                override fun onConnected(p0: Bundle?) {
                    logd(TAG, "Mobvoi client connected.")
                    c.unregisterConnectionCallbacks(this)
                    failListener?.let { c.unregisterConnectionFailedListener(it) }
                    connCallback = null
                    failListener = null
                    countDownLatch?.countDown()
                    cort.resume(true)
                }

                override fun onConnectionSuspended(p0: Int) {
                    logw(TAG, "Mobvoi client connection suspended.")
                    cort.resume(false)
                }
            }

            failListener = MobvoiApiClient.OnConnectionFailedListener { result ->
                logw(TAG, "Mobvoi client connection fail. errCode=${result.errorCode}")
                connCallback.let { c.unregisterConnectionCallbacks(it) }
                failListener?.let { c.unregisterConnectionFailedListener(it) }
                connCallback = null
                failListener = null
                countDownLatch?.countDown()
                cort.resume(false)
            }
            c.registerConnectionCallbacks(connCallback)
            c.registerConnectionFailedListener(failListener)
            countDownLatch = CountDownLatch(1)
            logd(TAG, "Try connect...")
            c.connect()
        }
    }

    private suspend fun checkConnect(c: MobvoiApiClient?): Boolean {
        if (c == null) {
            return false
        }
        if (c.isConnected) {
            return true
        }
        if (c.isConnecting) {
            return withContext(Dispatchers.IO) {
                logd(TAG, "Waiting connect...")
                try {
                    countDownLatch?.await(1000L, TimeUnit.MILLISECONDS)
                    c.isConnected
                } catch (e: InterruptedException) {
                    false
                }
            }
        }
        return false
    }

}