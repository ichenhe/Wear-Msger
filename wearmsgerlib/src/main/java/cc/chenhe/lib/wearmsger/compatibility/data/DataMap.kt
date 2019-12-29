package cc.chenhe.lib.wearmsger.compatibility.data

import android.os.Bundle
import cc.chenhe.lib.wearmsger.WM
import com.google.android.gms.wearable.DataMap

@Suppress("unused")
class DataMap {

    internal val gms: DataMap?
    internal val mms: com.mobvoi.android.wearable.DataMap?

    constructor(dataMap: DataMap) {
        gms = dataMap
        mms = null
    }

    constructor(dataMap: com.mobvoi.android.wearable.DataMap) {
        mms = dataMap
        gms = null
    }

    constructor() {
        when (WM.mode) {
            WM.MODE_GMS -> {
                gms = DataMap()
                mms = null
            }
            WM.MODE_MMS -> {
                mms = com.mobvoi.android.wearable.DataMap()
                gms = null
            }
            else -> throw IllegalStateException("Unknown mode, have you call init?")
        }
    }

    fun toBundle(): Bundle = gms?.toBundle() ?: mms!!.toBundle()

    fun toByteArray(): ByteArray = gms?.toByteArray() ?: mms!!.toByteArray()

    fun size() = gms?.size() ?: mms!!.size()

    fun isEmpty() = gms?.isEmpty ?: mms!!.isEmpty

    fun clear() = gms?.clear() ?: mms!!.clear()

    fun containsKey(key: String) = gms?.containsKey(key) ?: mms!!.containsKey(key)

    fun <T> get(key: String): T? {
        gms?.run {
            return get(key)
        }
        mms!!.let {
            return get(key)
        }
    }

    fun remove(key: String): Any? {
        gms?.run {
            return remove(key)
        }
        mms!!.run {
            return remove(key)
        }
    }

    fun putAll(dataMap: cc.chenhe.lib.wearmsger.compatibility.data.DataMap) =
        gms?.putAll(dataMap.gms!!) ?: mms?.putAll(dataMap.mms!!)

    fun keySet(): Set<String> {
        gms?.run {
            return keySet()
        }
        mms!!.run {
            return keySet()
        }
    }

    fun putBoolean(key: String, value: Boolean) =
        gms?.putBoolean(key, value) ?: mms!!.putBoolean(key, value)

    fun putByte(key: String, value: Byte) =
        gms?.putByte(key, value) ?: mms!!.putByte(key, value)

    fun putInt(key: String, value: Int) =
        gms?.putInt(key, value) ?: mms!!.putInt(key, value)

    fun putLong(key: String, value: Long) =
        gms?.putLong(key, value) ?: mms!!.putLong(key, value)

    fun putFloat(key: String, value: Float) =
        gms?.putFloat(key, value) ?: mms!!.putFloat(key, value)

    fun putDouble(key: String, value: Double) =
        gms?.putDouble(key, value) ?: mms!!.putDouble(key, value)

    fun putString(key: String, value: String) =
        gms?.putString(key, value) ?: mms!!.putString(key, value)

    fun putAsset(key: String, value: Asset) =
        gms?.putAsset(key, value.gms!!) ?: mms!!.putAsset(key, value.mms!!)

    fun putDataMap(key: String, value: cc.chenhe.lib.wearmsger.compatibility.data.DataMap) =
        gms?.putDataMap(key, value.gms!!) ?: mms!!.putDataMap(key, value.mms!!)

    fun putDataMapArrayList(
        key: String,
        value: ArrayList<cc.chenhe.lib.wearmsger.compatibility.data.DataMap>
    ) = gms?.putDataMapArrayList(key, ArrayList(value.map { it.gms!! }))
        ?: ArrayList(value.map { it.mms!! })

    fun putIntegerArrayList(key: String, value: ArrayList<Int>) =
        gms?.putIntegerArrayList(key, value) ?: mms!!.putIntegerArrayList(key, value)

    fun putStringArrayList(key: String, value: ArrayList<String>) =
        gms?.putStringArrayList(key, value) ?: mms!!.putStringArrayList(key, value)

    fun putByteArray(key: String, value: ByteArray) =
        gms?.putByteArray(key, value) ?: mms!!.putByteArray(key, value)

    fun putLongArray(key: String, value: LongArray) =
        gms?.putLongArray(key, value) ?: mms!!.putLongArray(key, value)

    fun putFloatArray(key: String, value: FloatArray) =
        gms?.putFloatArray(key, value) ?: mms!!.putFloatArray(key, value)

    fun putStringArray(key: String, value: Array<String>) =
        gms?.putStringArray(key, value) ?: mms!!.putStringArray(key, value)

    fun getBoolean(key: String, def: Boolean = false) =
        gms?.getBoolean(key, def) ?: mms!!.getBoolean(key, def)

    fun getByte(key: String, def: Byte = 0.toByte()) =
        gms?.getByte(key, def) ?: mms!!.getByte(key, def)

    fun getInt(key: String, def: Int = 0) =
        gms?.getInt(key, def) ?: mms!!.getInt(key, def)

    fun getLong(key: String, def: Long = 0) =
        gms?.getLong(key, def) ?: mms!!.getLong(key, def)

    fun getFloat(key: String, def: Float = 0f) =
        gms?.getFloat(key, def) ?: mms!!.getFloat(key, def)

    fun getDouble(key: String, def: Double = 0.0) =
        gms?.getDouble(key, def) ?: mms!!.getDouble(key, def)

    fun getString(key: String, def: String? = null): String? {
        gms?.run {
            return getString(key, def)
        }
        mms!!.run {
            return getString(key, def)
        }
    }

    fun getAsset(key: String): Asset? {
        gms?.run {
            return Asset(getAsset(key))
        }
        mms!!.run {
            return Asset(getAsset(key))
        }
    }

    fun getDataMap(key: String): cc.chenhe.lib.wearmsger.compatibility.data.DataMap? {
        gms?.run {
            return DataMap(getDataMap(key))
        }
        mms!!.run {
            return DataMap(getDataMap(key))
        }
    }

    fun getIntegerArrayList(key: String): ArrayList<Int> {
        gms?.run {
            return getIntegerArrayList(key)
        }
        mms!!.run {
            return getIntegerArrayList(key)
        }
    }

    fun getStringArrayList(key: String): ArrayList<String> {
        gms?.run {
            return getStringArrayList(key)
        }
        mms!!.run {
            return getStringArrayList(key)
        }
    }

    fun getDataMapArrayList(key: String): ArrayList<cc.chenhe.lib.wearmsger.compatibility.data.DataMap> {
        gms?.run {
            return ArrayList(getDataMapArrayList(key).map { DataMap(it) })
        }
        mms!!.run {
            return ArrayList(getDataMapArrayList(key).map { DataMap(it) })
        }
    }

    fun getByteArray(key: String): ByteArray {
        gms?.run {
            return getByteArray(key)
        }
        mms!!.run {
            return getByteArray(key)
        }
    }

    fun getLongArray(key: String): LongArray {
        gms?.run {
            return getLongArray(key)
        }
        mms!!.run {
            return getLongArray(key)
        }
    }

    fun getFloatArray(key: String): FloatArray {
        gms?.run {
            return getFloatArray(key)
        }
        mms!!.run {
            return getFloatArray(key)
        }
    }

    fun getStringArray(key: String): Array<String> {
        gms?.run {
            return getStringArray(key)
        }
        mms!!.run {
            return getStringArray(key)
        }
    }

    override operator fun equals(other: Any?): Boolean {
        if (other is cc.chenhe.lib.wearmsger.compatibility.data.DataMap) {
            gms?.let { return it == other.gms }
            mms?.let { return it == other.mms }
        }
        return false
    }

    override fun hashCode(): Int {
        return gms?.hashCode() ?: mms!!.hashCode() ?: super.hashCode()
    }

    override fun toString(): String {
        gms?.run {
            return toString()
        }
        mms!!.run {
            return toString()
        }
    }

    companion object {
        @JvmStatic
        fun fromBundle(bundle: Bundle): cc.chenhe.lib.wearmsger.compatibility.data.DataMap {
            return when (WM.mode) {
                WM.MODE_GMS -> DataMap(DataMap.fromBundle(bundle))
                WM.MODE_MMS -> DataMap(com.mobvoi.android.wearable.DataMap.fromBundle(bundle))
                else -> throw IllegalStateException("Unknown mode, have you call init?")
            }
        }

        @JvmStatic
        fun fromByteArray(bytes: ByteArray): cc.chenhe.lib.wearmsger.compatibility.data.DataMap {
            return when (WM.mode) {
                WM.MODE_GMS -> DataMap(DataMap.fromByteArray(bytes))
                WM.MODE_MMS -> DataMap(com.mobvoi.android.wearable.DataMap.fromByteArray(bytes)!!)
                else -> throw IllegalStateException("Unknown mode, have you call init?")
            }
        }

        @JvmStatic
        fun arrayListFromBundleArrayList(bundleList: ArrayList<Bundle>): List<cc.chenhe.lib.wearmsger.compatibility.data.DataMap> {
            return when (WM.mode) {
                WM.MODE_GMS -> DataMap.arrayListFromBundleArrayList(bundleList).map { DataMap(it) }
                WM.MODE_MMS -> com.mobvoi.android.wearable.DataMap.arrayListFromBundleArrayList(
                    bundleList
                ).map { DataMap(it) }
                else -> throw IllegalStateException("Unknown mode, have you call init?")
            }
        }
    }
}