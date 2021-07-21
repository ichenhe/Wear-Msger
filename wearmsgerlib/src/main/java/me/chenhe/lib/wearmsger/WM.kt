package me.chenhe.lib.wearmsger

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import me.chenhe.lib.wearmsger.compatibility.ClientCompat
import me.chenhe.lib.wearmsger.compatibility.GmsImpl

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
    return GmsImpl
}

internal fun logd(tag: String, a: Any) {
    Log.d("WearMsger", "[$tag] $a")
}

internal fun logw(tag: String, a: Any, e: Exception? = null) {
    Log.w("WearMsger", "[$tag] $a", e)
}

internal fun loge(tag: String, a: Any, e: Exception? = null) {
    Log.e("WearMsger", "[$tag] $a", e)
}

object WM {
    private const val TAG = "WearMsger"

    /**
     * 双向通讯等待超时时间（毫秒），包含了发送等待时间。
     */
    var bothWayTimeout = 3000L

    /**
     * 判断 GMS 服务在当前设备上是否可用。
     */
    fun isAvailable(context: Context): Boolean {
        val names = arrayOf(
            "com.google.android.gms",
            "com.google.android.wearable.app",
            "com.google.android.wearable.app.cn"
        )
        val pm = context.applicationContext.packageManager
        names.forEach {
            try {
                pm.getPackageInfo(it, 0)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                logd(TAG, "Fail to check the package $it")
            }
        }
        return false
    }

    /**
     * 判断当前是否是 ticwear 系统。
     */
    fun isTicwear(): Boolean {
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
