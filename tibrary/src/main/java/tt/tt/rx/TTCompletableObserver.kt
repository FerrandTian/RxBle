package tt.tt.rx

import io.reactivex.rxjava3.core.CompletableObserver
import io.reactivex.rxjava3.disposables.Disposable

/**
 * @author tianfeng
 */
abstract class TTCompletableObserver(var set: TTDisposables? = null) : CompletableObserver {
    var disposable: Disposable? = null

    override fun onSubscribe(d: Disposable) {
        disposable = d
        set?.add(d)
    }

    override fun onComplete() {
        dispose()
    }

    override fun onError(e: Throwable) {
        e.printStackTrace()
        dispose()
    }

    fun dispose() {
        set?.dispose(disposable)
        disposable?.dispose()
        disposable = null
    }
}