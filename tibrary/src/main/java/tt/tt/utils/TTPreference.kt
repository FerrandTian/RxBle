@file:JvmName("TTPreference")

package tt.tt.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.JsonParser
import com.google.gson.GsonBuilder
import java.util.ArrayList

/**
 * @author tianfeng
 */

lateinit var sp: SharedPreferences
    private set

var gson = GsonBuilder().create()

fun init(context: Context) {
    sp = PreferenceManager.getDefaultSharedPreferences(context)
}

fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
    sp.registerOnSharedPreferenceChangeListener(listener)

fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
    sp.unregisterOnSharedPreferenceChangeListener(listener)

fun contains(key: String): Boolean = sp.contains(key)

fun getBoolean(key: String, defValue: Boolean): Boolean = sp.getBoolean(key, defValue)

fun getInt(key: String, defValue: Int): Int = sp.getInt(key, defValue)

fun getFloat(key: String, defValue: Float): Float = sp.getFloat(key, defValue)

fun getLong(key: String, defValue: Long): Long = sp.getLong(key, defValue)

fun getString(key: String, defValue: String?): String? = sp.getString(key, defValue)

fun getStringSet(key: String, defValue: Set<String?>?): Set<String>? =
    sp.getStringSet(key, defValue)

fun <T> getList(key: String, tClass: Class<T>): List<T>? {
    val jsonStr = getString(key, "")
    if (jsonStr != null && jsonStr.isNotEmpty()) {
        val mList: MutableList<T> = ArrayList()
        val array = JsonParser().parse(jsonStr).asJsonArray
        for (elem in array) {
            mList.add(gson.fromJson(elem, tClass))
        }
        return mList
    }
    return null
}

fun <T> getObject(key: String, tClass: Class<T>): T? {
    val jsonStr = getString(key, "")
    return if (jsonStr != null && jsonStr.isNotEmpty()) gson.fromJson(jsonStr, tClass) else null
}

fun putObject(key: String, obj: Any?) =
    if (obj == null) remove(key) else putString(key, gson.toJson(obj))

fun putBoolean(key: String, value: Boolean) = sp.edit().putBoolean(key, value).apply()

fun putInt(key: String, value: Int) = sp.edit().putInt(key, value).apply()

fun putFloat(key: String, value: Float) = sp.edit().putFloat(key, value).apply()

fun putLong(key: String, value: Long) = sp.edit().putLong(key, value).apply()

fun putString(key: String, value: String?) = sp.edit().putString(key, value).apply()

fun putStringSet(key: String, values: Set<String?>?) =
    sp.edit().putStringSet(key, values).apply()

fun remove(key: String) = sp.edit().remove(key).apply()

fun clear() = sp.edit()?.clear()?.apply()