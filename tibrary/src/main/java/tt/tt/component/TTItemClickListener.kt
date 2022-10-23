package tt.tt.component

import android.view.View
import androidx.viewbinding.ViewBinding

/**
 * @author tianfeng
 */
interface TTItemClickListener<V : ViewBinding, T> {
    fun onItemClick(view: View, holder: TTHolder<V>, item: T)
}