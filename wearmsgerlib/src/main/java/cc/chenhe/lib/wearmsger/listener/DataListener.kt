package cc.chenhe.lib.wearmsger.listener

import android.net.Uri
import cc.chenhe.lib.wearmsger.compatibility.data.DataMapItem

interface DataListener {
    fun preProcessChanged(dataMapItem: DataMapItem) {
        onDataChanged(dataMapItem)
    }

    fun preProcessDeleted(uri: Uri) {
        onDataDeleted(uri)
    }

    fun onDataChanged(dataMapItem: DataMapItem)

    fun onDataDeleted(uri: Uri)

}