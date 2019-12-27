package cc.chenhe.lib.wearmsger.bean

data class MessageResult(
    /**
     * 发送结果。
     *
     * 注意，此结果只能表明是否提交到 GMS 或 MMS 服务，不能保证抵达目标设备。
     */
    val result: Int,
    /**
     * 发送失败则 id 固定为 0.
     */
    val requestId: Long
) {
    companion object {
        const val RESULT_OK = 0
        const val RESULT_FAIL = 1
        const val RESULT_INTERRUPTED = 2
        const val RESULT_TIMEOUT = 3
    }

    @Suppress("unused")
    fun isSuccess() = result == RESULT_OK
}