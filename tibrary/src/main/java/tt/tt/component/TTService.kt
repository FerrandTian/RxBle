package tt.tt.component

import android.app.Service
import android.content.Context
import tt.tt.rx.TTDisposables

/**
 * @author tianfeng
 */
abstract class TTService : Service() {
    val log = TTLog(this.javaClass.simpleName)
    val ctx: Context
        get() = this
    val disposables = TTDisposables()

    override fun onDestroy() {
        super.onDestroy()
        disposables.disposeAll()
    }
}