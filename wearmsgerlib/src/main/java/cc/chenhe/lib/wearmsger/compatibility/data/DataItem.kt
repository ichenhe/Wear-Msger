package cc.chenhe.lib.wearmsger.compatibility.data

import android.net.Uri
import com.google.android.gms.wearable.DataItem

@Suppress("unused")
class DataItem {
    private val gms: DataItem?
    private val mms: com.mobvoi.android.wearable.DataItem?

    constructor(dataItem: DataItem) {
        gms = dataItem
        mms = null
    }

    constructor(dataItem: com.mobvoi.android.wearable.DataItem) {
        mms = dataItem
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

    fun getAssets(): Map<String, DataItemAsset>? {
        gms?.let { gms ->
            return gms.assets?.mapValues { DataItemAsset(it.value) }
        }
        mms?.let { gms ->
            return gms.assets?.mapValues { DataItemAsset(it.value) }
        }
        return null
    }

    internal fun createDataMapItem(): DataMapItem {
        return gms?.let { DataMapItem(it) } ?: DataMapItem(mms!!)
    }

    internal fun createPutDataRequest(): PutDataRequest {
        return gms?.let { PutDataRequest(it) } ?: PutDataRequest(mms!!)
    }
}