package tt.tt.rx

import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable

/**
 * @author tianfeng
 */
abstract class TTSingleObserver<T>(var set: TTDisposables? = null) : SingleObserver<T> {
    var disposable: Disposable? = null

    override fun onSubscribe(d: Disposable) {
        disposable = d
        set?.add(d)
    }

    override fun onSuccess(t: T) {
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