package cc.chenhe.lib.wearmsger.compatibility.data

import android.net.Uri
import cc.chenhe.lib.wearmsger.WM
import com.google.android.gms.wearable.PutDataMapRequest

@Suppress("unused")
class PutDataMapRequest {
    internal val gms: PutDataMapRequest?
    internal val mms: com.mobvoi.android.wearable.PutDataMapRequest?

    constructor(putDataMapRequest: PutDataMapRequest) {
        gms = putDataMapRequest
        mms = null
    }

    constructor(putDataMapRequest: com.mobvoi.android.wearable.PutDataMapRequest) {
        mms = putDataMapRequest
        gms = null
    }

    internal constructor(dataMapItem: com.google.android.gms.wearable.DataMapItem) {
        gms = PutDataMapRequest.createFromDataMapItem(dataMapItem)
        mms = null
    }

    internal constructor(dataMapItem: com.mobvoi.android.wearable.DataMapItem) {
        mms = com.mobvoi.android.wearable.PutDataMapRequest.createFromDataMapItem(dataMapItem)
        gms = null
    }

    fun getUri(): Uri = gms?.uri ?: mms!!.uri

    fun getDataMap(): DataMap {
        gms?.run {
            return DataMap(dataMap)
        }
        mms!!.run {
            return DataMap(dataMap)
        }
    }

    fun setUrgent(): cc.chenhe.lib.wearmsger.compatibility.data.PutDataMapRequest {
        gms?.setUrgent() ?: mms!!.setUrgent()
        return this
    }

    fun isUrgent() = gms?.isUrgent ?: mms!!.isUrgent

    fun asPutDataRequest(): PutDataRequest {
        return gms?.let { PutDataRequest(it.asPutDataRequest()) }
            ?: PutDataRequest(mms!!.asPutDataRequest())
    }

    companion object {
        @JvmStatic
        fun createFromDataMapItem(dataMapItem: DataMapItem): cc.chenhe.lib.wearmsger.compatibility.data.PutDataMapRequest {
            return dataMapItem.createPutDataMapRequest()
        }

        @JvmStatic
        fun createWithAutoAppendedId(path: String): cc.chenhe.lib.wearmsger.compatibility.data.PutDataMapRequest {
            return when (WM.mode) {
                WM.MODE_GMS -> PutDataMapRequest(PutDataMapRequest.createWithAutoAppendedId(path))
                WM.MODE_MMS -> PutDataMapRequest(
                    com.mobvoi.android.wearable.PutDataMapRequest.createWithAutoAppendedId(
                        path
                    )
                )
                else -> throw IllegalStateException("Unknown mode, have you call init?")
            }
        }

        @JvmStatic
        fun create(path: String): cc.chenhe.lib.wearmsger.compatibility.data.PutDataMapRequest {
            return when (WM.mode) {
                WM.MODE_GMS -> PutDataMapRequest(PutDataMapRequest.create(path))
                WM.MODE_MMS -> PutDataMapRequest(
                    com.mobvoi.android.wearable.PutDataMapRequest.create(path)
                )
                else -> throw IllegalStateException("Unknown mode, have you call init?")
            }
        }
    }
}