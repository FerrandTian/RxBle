package tt.tt.component

import android.app.Service
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import tt.tt.rx.TTDisposables

/**
 * @author tianfeng
 */
abstract class TTService : Service() {
    val log = TTLog(this.javaClass.simpleName)
    val context: Context
        get() = this
    val disposables = TTDisposables()

    @JvmOverloads
    fun toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, resId, duration).show()
    }

    @JvmOverloads
    fun toast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, text, duration).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.disposeAll()
    }
}