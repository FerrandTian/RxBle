package tt.tt.rx

import io.reactivex.rxjava3.disposables.Disposable

/**
 * @author tianfeng
 */
class TTDisposables {
    private val set: MutableSet<Disposable> = HashSet()

    fun add(d: Disposable) = set.add(d)

    fun dispose(d: Disposable?) = set.remove(d).also {
        d?.dispose()
    }

    fun disposeAll() {
        set.removeAll {
            it.dispose()
            true
        }
    }
}