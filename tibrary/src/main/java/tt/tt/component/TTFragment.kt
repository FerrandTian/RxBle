package tt.tt.component

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import tt.tt.rx.TTDisposables

/**
 * @author tianfeng
 */
abstract class TTFragment : Fragment() {
    val log = TTLog(this.javaClass.simpleName)
    val disposables = TTDisposables()
    protected lateinit var viewModelProvider: ViewModelProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModelProvider = ViewModelProvider(this)
    }

    @JvmOverloads
    fun toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, resId, duration).show()
    }

    @JvmOverloads
    fun toast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, text, duration).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.disposeAll()
    }
}