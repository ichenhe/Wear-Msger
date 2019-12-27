package cc.chenhe.lib.wearmsger

import android.content.Context
import android.util.Log
import cc.chenhe.lib.wearmsger.compatibility.ClientCompat
import cc.chenhe.lib.wearmsger.compatibility.GmsImpl
import cc.chenhe.lib.wearmsger.compatibility.MmsImpl
import com.mobvoi.android.common.MobvoiApiManager
import com.mobvoi.android.common.api.MobvoiApiClient
import com.mobvoi.android.wearable.Wearable

/**
 * 发送请求超时时长，指的是提交到 GMS 而不是到达目标设备。
 */
internal const val SEND_TIMEOUT: Long = 3000L

/**
 * 传输 Data 时附加 ID 所在的字段。
 */
internal const val DATA_ID_KEY = "WearMsger_ID"

internal fun getClient(): ClientCompat {
    return when (WM.mode) {
        WM.MODE_GMS -> GmsImpl
        WM.MODE_MMS -> MmsImpl
        else -> throw IllegalStateException("Please init before use")
    }
}

internal fun logd(tag: String, a: Any) {
    Log.d("WearMsger", "[$tag] $a")
}

internal fun logw(tag: String, a: Any) {
    Log.w("WearMsger", "[$tag] $a")
}

internal fun loge(tag: String, a: Any) {
    Log.e("WearMsger", "[$tag] $a")
}

object WM {
    const val MODE_GMS = 0
    const val MODE_MMS = 1
    const val MODE_UNKNOWN = -1

    var mode = MODE_UNKNOWN
        private set

    internal var mobvoiApiClient: MobvoiApiClient? = null
        private set

    /**
     * 初始化。
     * @throws IllegalStateException 设备不支持指定模式时抛出此异常。
     */
    @JvmStatic
    @Suppress("unused")
    fun init(context: Context, mode: Mode) {
        when (mode) {
            Mode.GMS -> initGMS(context)
            Mode.MMS -> initMMS(context)
            Mode.AUTO -> autoDetect(context)
        }
    }

    private fun initGMS(context: Context, check: Boolean = false) {
        mobvoiApiClient?.disconnect()
        mobvoiApiClient = null
        if (check || MobvoiApiManager.getInstance().isGmsAvailable(context)) {
            this.mode = MODE_GMS
        } else {
            throw IllegalStateException("GMS not supported")
        }
    }

    private fun initMMS(context: Context, check: Boolean = false) {
        if (check || MobvoiApiManager.getInstance().isMmsAvailable(context)) {
            this.mode = MODE_MMS
            if (!MobvoiApiManager.getInstance().isInitialized) {
                MobvoiApiManager.getInstance().loadService(
                    context.applicationContext,
                    MobvoiApiManager.ApiGroup.MMS
                )
                mobvoiApiClient = MobvoiApiClient.Builder(context.applicationContext)
                    .addApi(Wearable.API)
                    .build()
                mobvoiApiClient?.connect()
            }
        } else {
            throw IllegalStateException("MMS not supported")
        }
    }

    private fun autoDetect(context: Context) {
        when {
            MobvoiApiManager.getInstance().isGmsAvailable(context) -> {
                initGMS(context, true)
            }
            MobvoiApiManager.getInstance().isMmsAvailable(context) -> {
                initMMS(context, true)
            }
            else -> {
                throw IllegalStateException("None GMS or MMS supported")
            }
        }
    }

    enum class Mode {
        /**
         * Ticwear 系统所使用的 api.
         */
        GMS,
        /**
         * WearOS 系统所使用的 api.
         */
        MMS,
        /**
         * 自动判断类型。
         */
        AUTO
    }
}
