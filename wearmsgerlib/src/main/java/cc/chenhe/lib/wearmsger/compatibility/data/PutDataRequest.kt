package cc.chenhe.lib.wearmsger.compatibility.data

import android.net.Uri
import cc.chenhe.lib.wearmsger.WM
import com.google.android.gms.wearable.PutDataRequest

@Suppress("unused")
class PutDataRequest {

    private val gms: PutDataRequest?
    private val mms: com.mobvoi.android.wearable.PutDataRequest?

    constructor(putDataRequest: PutDataRequest) {
        gms = putDataRequest
        mms = null
    }

    constructor(putDataRequest: com.mobvoi.android.wearable.PutDataRequest) {
        mms = putDataRequest
        gms = null
    }

    internal constructor(dataItem: com.google.android.gms.wearable.DataItem) {
        gms = PutDataRequest.createFromDataItem(dataItem)
        mms = null
    }

    internal constructor(dataItem: com.mobvoi.android.wearable.DataItem) {
        mms = com.mobvoi.android.wearable.PutDataRequest.createFromDataItem(dataItem)
        gms = null
    }

    fun getUri(): Uri = gms?.uri ?: mms!!.uri

    fun getData(): ByteArray? {
        gms?.run {
            return data
        }
        mms!!.run {
            return data
        }
    }

    fun setData(data: ByteArray) {
        gms?.data = data
        mms?.data = data
    }

    fun getAssets(): Map<String, Asset> {
        return gms?.run {
            assets?.mapValues { Asset(it.value) } ?: mapOf()
        } ?: mms!!.run {
            assets?.mapValues { Asset(it.value) } ?: mapOf()
        }
    }

    fun getAsset(key: String) =
        gms?.getAsset(key)?.let { Asset(it) } ?: mms!!.getAsset(key)?.let { Asset(it) }

    fun hasAsset(key: String) = gms?.hasAsset(key) ?: mms!!.hasAsset(key)

    fun putAsset(key: String, asset: Asset) {
        gms?.putAsset(key, asset.gms!!)
        mms?.putAsset(key, asset.mms!!)
    }

    fun removeAsset(key: String) {
        gms?.removeAsset(key)
        mms?.removeAsset(key)
    }

    fun isUrgent() = gms?.isUrgent ?: mms!!.isUrgent

    fun setUrgent() = gms?.setUrgent() ?: mms!!.setUrgent()

    override fun toString() = gms?.toString() ?: mms!!.toString()

    fun toString(verbose: Boolean) = gms?.toString(verbose) ?: mms!!.toString()

    companion object {
        @JvmStatic
        fun createFromDataItem(dataItem: DataItem): cc.chenhe.lib.wearmsger.compatibility.data.PutDataRequest {
            return dataItem.createPutDataRequest()
        }

        @JvmStatic
        fun createWithAutoAppendedId(path: String): cc.chenhe.lib.wearmsger.compatibility.data.PutDataRequest {
            return when (WM.mode) {
                WM.MODE_GMS -> PutDataRequest(PutDataRequest.createWithAutoAppendedId(path))
                WM.MODE_MMS -> PutDataRequest(
                    com.mobvoi.android.wearable.PutDataRequest.createWithAutoAppendedId(path)
                )
                else -> throw IllegalStateException("Unknown mode, have you call init?")
            }
        }

        @JvmStatic
        fun create(path: String): cc.chenhe.lib.wearmsger.compatibility.data.PutDataRequest {
            return when (WM.mode) {
                WM.MODE_GMS -> PutDataRequest(PutDataRequest.create(path))
                WM.MODE_MMS -> PutDataRequest(
                    com.mobvoi.android.wearable.PutDataRequest.create(path)
                )
                else -> throw IllegalStateException("Unknown mode, have you call init?")
            }
        }
    }
}