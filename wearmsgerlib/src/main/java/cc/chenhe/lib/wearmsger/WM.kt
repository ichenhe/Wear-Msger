package cc.chenhe.lib.wearmsger

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import cc.chenhe.lib.wearmsger.compatibility.ClientCompat
import cc.chenhe.lib.wearmsger.compatibility.GmsImpl
import cc.chenhe.lib.wearmsger.compatibility.MmsImpl
import com.mobvoi.android.common.MobvoiApiManager

/**
 * 发送请求超时时长，指的是提交到 GMS 而不是到达目标设备。
 */
internal const val SEND_TIMEOUT: Long = 3000L

/**
 * 传输 Data 时附加 ID 所在的字段。
 */
internal const val DATA_ID_KEY = "WearMsger_ID"

/**
 * 响应 path 前缀。
 * 完整格式为 /WMRE/{reqId}{path}
 */
internal const val WMRE = "/WMRE"

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

    private const val TAG = "WearMsger"

    var mode = MODE_UNKNOWN
        private set

    /**
     * 双向通讯等待超时时间（毫秒），包含了发送等待时间。
     */
    var bothWayTimeout = 3000L

    /**
     * 初始化。
     * @throws IllegalStateException 设备不支持指定模式时抛出此异常。
     */
    @JvmStatic
    @Suppress("unused")
    fun init(context: Context, mode: Mode) {
        if (this.mode != MODE_UNKNOWN) {
            getClient().removeAllListeners(context)
        }
        when (mode) {
            Mode.GMS -> initGMS(context)
            Mode.MMS -> initMMS(context)
            Mode.AUTO -> autoDetect(context)
        }
    }

    private fun initGMS(context: Context, check: Boolean = false) {
        MmsClientManager.destroy()
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
            }
        } else {
            throw IllegalStateException("MMS not supported")
        }
    }

    private fun autoDetect(context: Context) {
        val isWatch = context.packageManager.hasSystemFeature("android.hardware.type.watch")
        if (isWatch && isTicwear()) {
            logd(TAG, "Detect ticwear in watch device, use MMS mode.")
            initMMS(context, true)
        } else if (MobvoiApiManager.getInstance().isGmsAvailable(context)) {
            logd(TAG, "No ticwear rom and GMS is available, use GMS mode.")
            initGMS(context, true)
        } else if (MobvoiApiManager.getInstance().isMmsAvailable(context)) {
            logd(TAG, "Only MMS is available, use MMS mode.")
            initMMS(context, true)
        } else {
            throw IllegalStateException("None GMS or MMS supported")
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

    private fun isTicwear(): Boolean {
        var value = "unknown"
        try {
            @SuppressLint("PrivateApi")
            val c = Class.forName("android.os.SystemProperties")
            val get = c.getMethod("get", String::class.java, String::class.java)
            value = get.invoke(c, "ticwear.version.name", "unknown") as String
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return value != "unknown"
    }
}
