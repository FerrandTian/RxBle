package tk.limt.demo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

/**
 * @author tianfeng
 */
abstract class Adapter<B : ViewBinding, T>(
    protected val items: MutableList<T> = ArrayList(),
    protected var clickListener: OnItemClickListener<B, T>? = null,
) : RecyclerView.Adapter<Holder<B>>(), MutableList<T> by items {
    protected lateinit var context: Context

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder<B> {
        context = parent.context
        val type = javaClass.genericSuperclass as ParameterizedType
        val clazz = type.actualTypeArguments[0] as Class<*>
        val method = clazz.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.javaPrimitiveType
        )
        return Holder(method.invoke(null, LayoutInflater.from(context), parent, false) as B)
    }

    protected fun setClickListener(holder: Holder<B>, item: T, vararg views: View) {
        for (v in views) v.setOnClickListener {
            clickListener?.onItemClick(it, holder, item)
        }
    }
}