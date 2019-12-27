package cc.chenhe.lib.wearmsger.compatibility.data

import com.google.android.gms.wearable.DataItemAsset

@Suppress("unused")
class DataItemAsset {
    private val gms: DataItemAsset?
    private val mms: com.mobvoi.android.wearable.DataItemAsset?

    constructor(dataItemAsset: DataItemAsset) {
        gms = dataItemAsset
        mms = null
    }

    constructor(dataItemAsset: com.mobvoi.android.wearable.DataItemAsset) {
        mms = dataItemAsset
        gms = null
    }

    fun getId(): String = gms?.id ?: mms!!.id

    fun getDataItemKey(): String = gms?.dataItemKey ?: mms!!.dataItemKey
}