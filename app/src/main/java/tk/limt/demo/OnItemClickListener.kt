package tk.limt.demo

import android.view.View
import androidx.viewbinding.ViewBinding

/**
 * @author tianfeng
 */
interface OnItemClickListener<B : ViewBinding, T> {
    fun onItemClick(view: View, holder: Holder<B>, item: T)
}