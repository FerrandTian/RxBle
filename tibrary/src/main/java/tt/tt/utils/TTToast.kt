@file:JvmName("TTToast")

package tt.tt.utils

import android.content.Context
import android.widget.Toast

/**
 * @author tianfeng
 */
/**
 * 显示Toast，时间长度为[Toast.LENGTH_SHORT]
 *
 * @param context The context to use.  Usually your [android.app.Application]
 * or [android.app.Activity] object.
 * @param msg     The text to show. Can be formatted text.
 */
fun s(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

/**
 * 显示Toast，时间长度为[Toast.LENGTH_SHORT]
 *
 * @param context The context to use.  Usually your [android.app.Application]
 * or [android.app.Activity] object.
 * @param resId   The resource id of the string resource to use.  Can be formatted text.
 */
fun s(context: Context, resId: Int) {
    Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
}

/**
 * 显示Toast，时间长度为[Toast.LENGTH_LONG]
 *
 * @param context The context to use.  Usually your [android.app.Application]
 * or [android.app.Activity] object.
 * @param msg     The text to show. Can be formatted text.
 */
fun l(context: Context, msg: String) {
   Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
}

/**
 * 显示Toast，时间长度为[Toast.LENGTH_LONG]
 *
 * @param context The context to use.  Usually your [android.app.Application]
 * or [android.app.Activity] object.
 * @param resId   The resource id of the string resource to use.  Can be formatted text.
 */
fun l(context: Context, resId: Int) {
    Toast.makeText(context, resId, Toast.LENGTH_LONG).show()
}

