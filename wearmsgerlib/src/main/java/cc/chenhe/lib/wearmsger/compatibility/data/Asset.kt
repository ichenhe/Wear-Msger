package cc.chenhe.lib.wearmsger.compatibility.data

import android.net.Uri
import android.os.ParcelFileDescriptor
import cc.chenhe.lib.wearmsger.WM
import com.google.android.gms.wearable.Asset

@Suppress("unused")
class Asset {

    internal val gms: Asset?
    internal val mms: com.mobvoi.android.wearable.Asset?

    constructor(asset: Asset) {
        gms = asset
        mms = null
    }

    constructor(asset: com.mobvoi.android.wearable.Asset) {
        mms = asset
        gms = null
    }

    fun getData(): ByteArray = gms?.data ?: mms!!.data

    fun getDigest(): String = gms?.digest ?: mms!!.digest

    fun getFd(): ParcelFileDescriptor = gms?.fd ?: mms!!.fd

    fun getUri(): Uri = gms?.uri ?: mms!!.uri

    override fun hashCode(): Int = gms?.hashCode() ?: mms!!.hashCode()

    override operator fun equals(other: Any?): Boolean {
        if (other is cc.chenhe.lib.wearmsger.compatibility.data.Asset) {
            gms?.let { return it == other.gms }
            mms?.let { return it == other.mms }
        }
        return false
    }

    override fun toString(): String = gms?.toString() ?: mms!!.toString()

    companion object {
        fun createFromRef(digest: String): cc.chenhe.lib.wearmsger.compatibility.data.Asset {
            return when (WM.mode) {
                WM.MODE_GMS -> Asset(Asset.createFromRef(digest))
                WM.MODE_MMS -> Asset(com.mobvoi.android.wearable.Asset.createFromRef(digest))
                else -> throw IllegalStateException("Unknown mode, have you call init?")
            }
        }

        fun createFromBytes(assetData: ByteArray): cc.chenhe.lib.wearmsger.compatibility.data.Asset {
            return when (WM.mode) {
                WM.MODE_GMS -> Asset(Asset.createFromBytes(assetData))
                WM.MODE_MMS -> Asset(com.mobvoi.android.wearable.Asset.createFromBytes(assetData))
                else -> throw IllegalStateException("Unknown mode, have you call init?")
            }
        }

        fun createFromFd(fd: ParcelFileDescriptor): cc.chenhe.lib.wearmsger.compatibility.data.Asset {
            return when (WM.mode) {
                WM.MODE_GMS -> Asset(Asset.createFromFd(fd))
                WM.MODE_MMS -> Asset(com.mobvoi.android.wearable.Asset.createFromFd(fd))
                else -> throw IllegalStateException("Unknown mode, have you call init?")
            }
        }

        fun createFromUri(uri: Uri): cc.chenhe.lib.wearmsger.compatibility.data.Asset {
            return when (WM.mode) {
                WM.MODE_GMS -> Asset(Asset.createFromUri(uri))
                WM.MODE_MMS -> Asset(com.mobvoi.android.wearable.Asset.createFromUri(uri))
                else -> throw IllegalStateException("Unknown mode, have you call init?")
            }
        }

    }

}