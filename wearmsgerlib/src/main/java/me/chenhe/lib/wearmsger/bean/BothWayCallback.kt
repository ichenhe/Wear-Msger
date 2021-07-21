package me.chenhe.lib.wearmsger.bean

import com.google.android.gms.wearable.DataMapItem
import java.nio.charset.Charset

/**
 * BothWayRequest 得到的响应。
 */
interface BothWayCallback {
    val result: Result

    /**
     * 响应来源节点 id，失败则为 `null`.
     */
    val responseNodeId: String?

    enum class Result {
        OK,
        REQUEST_FAIL,
        TIMEOUT
    }

    @Suppress("unused")
    fun isSuccess() = result == Result.OK
}

/**
 * 得到的 message 响应。
 */
data class MessageCallback(
    override val result: BothWayCallback.Result,
    override val responseNodeId: String? = null,
    val data: ByteArray? = null
) : BothWayCallback {

    companion object {
        fun ok(responseNodeId: String?, data: ByteArray?): MessageCallback =
            MessageCallback(BothWayCallback.Result.OK, responseNodeId, data)
    }

    /**
     * 将 [data] 转为 [String]，使用 utf8 编码。
     */
    @Suppress("unused")
    fun getStringData(): String? = data?.let { String(it, Charset.forName("utf8")) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageCallback

        if (result != other.result) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = result.hashCode()
        result1 = 31 * result1 + (data?.contentHashCode() ?: 0)
        return result1
    }
}

/**
 * 得到的 data 响应。
 *
 * @param dataMapItem 若请求失败则为 `null`.
 */
data class DataCallback(
    override val result: BothWayCallback.Result,
    override val responseNodeId: String? = null,
    val dataMapItem: DataMapItem? = null
) : BothWayCallback {
    companion object {
        fun ok(responseNodeId: String?, dataMapItem: DataMapItem?): DataCallback =
            DataCallback(BothWayCallback.Result.OK, responseNodeId, dataMapItem)
    }
}