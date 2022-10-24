package tt.tt.component

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import tt.tt.rx.TTDisposables

/**
 * @author tianfeng
 */
class TTHolder<VB : ViewBinding>(val vb: VB) : RecyclerView.ViewHolder(vb.root) {
    val disposables = TTDisposables()

    companion object {
        fun type(clazz: Class<out ViewBinding>?): Int {
            return clazz?.hashCode() ?: RecyclerView.INVALID_TYPE
        }
    }
}