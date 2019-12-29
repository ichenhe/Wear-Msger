package cc.chenhe.lib.wearmsger.bean

import java.nio.charset.Charset

data class MessageEvent(
    val sourceNodeId: String,
    val requestId: Int,
    val path: String,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageEvent

        if (sourceNodeId != other.sourceNodeId) return false
        if (requestId != other.requestId) return false
        if (path != other.path) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sourceNodeId.hashCode()
        result = 31 * result + requestId
        result = 31 * result + path.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    /**
     * 将 [data] 转为 [String]，使用 utf8 编码。
     */
    @Suppress("unused")
    fun getStringData() = String(data, Charset.forName("utf8"))
}

fun com.google.android.gms.wearable.MessageEvent.toCompat(): MessageEvent {
    return MessageEvent(
        sourceNodeId,
        requestId,
        path,
        data
    )
}

fun com.mobvoi.android.wearable.MessageEvent.toCompat(): MessageEvent {
    return MessageEvent(
        sourceNodeId,
        requestId,
        path,
        data
    )
}