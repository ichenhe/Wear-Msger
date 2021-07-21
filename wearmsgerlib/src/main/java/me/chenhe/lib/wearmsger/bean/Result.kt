package me.chenhe.lib.wearmsger.bean

import kotlinx.coroutines.CancellationException

data class Result(
    /**
     * 发送结果。
     *
     * 注意，此结果只能表明是否提交到 GMS 或 MMS 服务，不能保证抵达目标设备。
     */
    val result: Int,
    /**
     * 发送失败或没有生成 id 则为0.
     */
    val requestId: Long = 0,
    val exception: Exception? = null,
) {
    companion object {
        const val RESULT_OK = 0
        const val RESULT_CANCELED = 4
        const val RESULT_TIMEOUT = 3
        const val RESULT_FAILED = 5

        fun ok(id: Int): Result = Result(RESULT_OK, id.toLong())

        fun ok(id: Long): Result = Result(RESULT_OK, id)

        fun canceled(e: CancellationException): Result = Result(RESULT_CANCELED, 0, e)

        fun failed(e: Exception): Result = Result(RESULT_FAILED, 0, e)
    }

    val isSuccess: Boolean get() = result == RESULT_OK
}