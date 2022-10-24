@file:JvmName("TTContext")

package tt.tt.utils

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat

fun Context.hasSystemFeature(featureName: String) = packageManager.hasSystemFeature(featureName)

val Context.bluetoothEnabled: Boolean
    get() = getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled == true

val Context.locationEnabled: Boolean
    get() = getSystemService(LocationManager::class.java)?.let {
        it.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    } ?: false

fun Context.permissionGranted(permission: String) = ActivityCompat.checkSelfPermission(
    this, permission
) == PackageManager.PERMISSION_GRANTED

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

fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}