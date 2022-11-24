package tt.tt.component

import android.view.View
import androidx.viewbinding.ViewBinding

/**
 * @author tianfeng
 */
interface TTOnLongClickListener<B : ViewBinding, T> {
    fun onLongClick(v: View, h: TTHolder<B>, t: T?): Boolean
}