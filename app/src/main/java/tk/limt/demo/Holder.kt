package tk.limt.demo

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * @author tianfeng
 */
class Holder<B : ViewBinding>(@JvmField val binding: B) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun type(clazz: Class<out ViewBinding>?): Int {
            return clazz?.hashCode() ?: RecyclerView.INVALID_TYPE
        }
    }
}