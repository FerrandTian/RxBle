package tt.tt.component

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import tt.tt.rx.TTDisposables

/**
 * @author tianfeng
 */
abstract class TTFragment : Fragment() {
    val log = TTLog(this.javaClass.simpleName)
    val disposables = TTDisposables()
    val ctx: FragmentActivity
        get() = requireActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.disposeAll()
    }
}