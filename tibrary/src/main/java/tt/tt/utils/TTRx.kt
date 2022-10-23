@file:JvmName("TTRx")

package tt.tt.utils

import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.disposables.Disposable

/**
 * @author tianfeng
 */
fun isDisposed(d: Disposable?): Boolean = d?.isDisposed ?: true

fun isDisposed(e: ObservableEmitter<*>?): Boolean = e?.isDisposed ?: true

fun dispose(vararg disposables: Disposable?) {
    for (d in disposables) if (d != null && !d.isDisposed) d.dispose()
}

fun dispose(disposables: Collection<Disposable?>?) {
    if (disposables != null) for (d in disposables) if (d != null && !d.isDisposed) d.dispose()
}