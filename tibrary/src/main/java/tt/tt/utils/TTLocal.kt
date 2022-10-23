@file:JvmName("TTLocal")

package tt.tt.utils

import android.content.Context
import android.os.Build
import android.os.LocaleList
import java.util.*

/**
 * @author tianfeng
 */
var systemLocale: Locale = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) LocaleList.getDefault().get(0) else Locale.getDefault())
    private set
var currentLocale: Locale = systemLocale
    private set

fun setLocale(context: Context, locale: Locale) {
    currentLocale = locale
    val res = context.resources
    val dm = res.displayMetrics
    val conf = res.configuration
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) conf.setLocale(currentLocale) else conf.locale = currentLocale
    res.updateConfiguration(conf, dm)
}

fun getDisplayPrice(price: Double): String = String.format(Locale.getDefault(), "%.2f", price)

fun getCurrencySign(currency: String?): String = when (currency) {
    "CNY" -> "￥"
    "USD" -> "＄"
    else -> ""
}