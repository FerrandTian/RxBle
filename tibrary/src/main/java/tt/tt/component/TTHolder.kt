package tt.tt.component

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * @author tianfeng
 */
class TTHolder<VB : ViewBinding>(@JvmField val vb: VB) : RecyclerView.ViewHolder(vb.root) {

    companion object {
        fun type(clazz: Class<out ViewBinding>?): Int {
            return clazz?.hashCode() ?: RecyclerView.INVALID_TYPE
        }
    }
}