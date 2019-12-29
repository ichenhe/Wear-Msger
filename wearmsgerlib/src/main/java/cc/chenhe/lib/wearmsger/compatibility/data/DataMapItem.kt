package cc.chenhe.lib.wearmsger.compatibility.data

import android.net.Uri
import com.google.android.gms.wearable.DataMapItem

@Suppress("unused")
class DataMapItem {

    private val gms: DataMapItem?
    private val mms: com.mobvoi.android.wearable.DataMapItem?

    constructor(dataMapItem: DataMapItem) {
        gms = dataMapItem
        mms = null
    }

    constructor(dataMapItem: com.mobvoi.android.wearable.DataMapItem) {
        mms = dataMapItem
        gms = null
    }

    internal constructor(dataItem: com.google.android.gms.wearable.DataItem) {
        gms = DataMapItem.fromDataItem(dataItem)
        mms = null
    }

    internal constructor(dataItem: com.mobvoi.android.wearable.DataItem) {
        mms = com.mobvoi.android.wearable.DataMapItem.fromDataItem(dataItem)
        gms = null
    }

    companion object {
        fun fromDataItem(dataItem: DataItem): cc.chenhe.lib.wearmsger.compatibility.data.DataMapItem {
            return dataItem.createDataMapItem()
        }
    }

    fun getUri(): Uri = gms?.uri ?: mms!!.uri

    fun getDataMap() = gms?.dataMap?.let { DataMap(it) } ?: DataMap(mms!!.dataMap)

    internal fun createPutDataMapRequest(): PutDataMapRequest {
        return gms?.let { PutDataMapRequest(it) } ?: PutDataMapRequest(mms!!)
    }
}