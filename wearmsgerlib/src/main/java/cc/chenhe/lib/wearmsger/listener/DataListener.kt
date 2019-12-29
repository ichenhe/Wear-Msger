package cc.chenhe.lib.wearmsger.listener

import android.net.Uri
import androidx.annotation.MainThread
import cc.chenhe.lib.wearmsger.WMRE
import cc.chenhe.lib.wearmsger.compatibility.data.DataMapItem

interface DataListener {
    @MainThread
    fun preProcessChanged(dataMapItem: DataMapItem) {
        // 过滤掉 BothWay 的响应，其应当交给对应的 Callback 处理。
        if (dataMapItem.getUri().path?.startsWith("$WMRE/") == false)
            onDataChanged(dataMapItem)
    }

    @MainThread
    fun preProcessDeleted(uri: Uri) {
        if (uri.path?.startsWith("$WMRE/") == false)
            onDataDeleted(uri)
    }

    @MainThread
    fun onDataChanged(dataMapItem: DataMapItem)

    @MainThread
    fun onDataDeleted(uri: Uri)

}