package tk.limt.demo

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.TypedValue
import androidx.core.app.ActivityCompat

/**
 * @author tianfeng
 */
fun ByteArray.hex() = if (this.isEmpty()) "" else {
    val buf = StringBuilder(size * 2)
    for (b in this) buf.append(String.format("%02x", b.toInt() and 0xff))
    buf.toString()
}

fun ByteArray.string() = this.decodeToString().trimEnd(Char.MIN_VALUE)

fun Context.isLocationEnabled(): Boolean {
    val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val gps = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val network = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    return gps || network
}

fun Context.checkPermission(permission: String) = ActivityCompat.checkSelfPermission(
    this, permission
) != PackageManager.PERMISSION_GRANTED

fun Context.dp2px(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics
)

fun Context.sp2px(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics
)

fun Context.pt2px(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_PT, value, resources.displayMetrics
)

fun Context.in2px(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_IN, value, resources.displayMetrics
)

fun Context.mm2px(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_MM, value, resources.displayMetrics
)