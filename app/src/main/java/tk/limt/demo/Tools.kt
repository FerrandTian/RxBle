package tk.limt.demo

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.TypedValue
import android.view.View
import android.widget.PopupWindow
import androidx.core.app.ActivityCompat

/**
 * @author tianfeng
 */
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

fun activate(activated: Boolean, vararg views: View) {
    for (v in views) v.isActivated = activated
}

fun clickable(clickable: Boolean, vararg views: View) {
    for (v in views) v.isClickable = clickable
}

fun enable(enabled: Boolean, vararg views: View) {
    for (v in views) v.isEnabled = enabled
}

fun clickListener(l: View.OnClickListener?, vararg views: View) {
    for (v in views) v.setOnClickListener(l)
}

fun longClickListener(l: View.OnLongClickListener?, vararg views: View) {
    for (v in views) v.setOnLongClickListener(l)
}

fun select(selected: Boolean, vararg views: View) {
    for (v in views) v.isSelected = selected
}

fun <T : View> visible(vararg views: T) {
    for (v in views) v.visibility = View.VISIBLE
}

fun <T : View> invisible(vararg views: T) {
    invisible(false, *views)
}

fun <T : View> invisible(visible: Boolean, vararg views: T) {
    for (v in views) v.visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

fun <T : View> gone(vararg views: T) {
    gone(false, *views)
}

fun <T : View> gone(visible: Boolean, vararg views: T) {
    for (v in views) v.visibility = if (visible) View.VISIBLE else View.GONE
}

fun isVisible(view: View?): Boolean = if (view != null) view.visibility == View.VISIBLE else false

fun isShowing(dialog: Dialog?): Boolean = dialog?.isShowing ?: false

fun dismiss(vararg dialogs: Dialog?) {
    for (dialog in dialogs) if (dialog != null && dialog.isShowing) dialog.dismiss()
}

fun cancel(vararg dialogs: Dialog?) {
    for (dialog in dialogs) dialog?.cancel()
}

fun isShowing(window: PopupWindow?): Boolean = window?.isShowing ?: false

fun dismiss(vararg windows: PopupWindow?) {
    for (window in windows) if (window != null && window.isShowing) window.dismiss()
}