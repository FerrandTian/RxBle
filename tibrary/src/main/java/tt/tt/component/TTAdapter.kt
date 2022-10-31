/*
 * Copyright (C) 2022 TianFeng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tt.tt.component

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
abstract class TTAdapter<B : ViewBinding, T>(
    protected val items: MutableList<T> = ArrayList()
) : RecyclerView.Adapter<TTHolder<*>>(), MutableList<T> by items {
    val parameterizedType = javaClass.genericSuperclass as ParameterizedType
    val viewBindingClass = parameterizedType.actualTypeArguments[0] as Class<out B>
    val inflateMethod = viewBindingClass.getDeclaredMethod(
        "inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.javaPrimitiveType
    )
    lateinit var ctx: Context
    lateinit var recycler: RecyclerView

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return TTHolder.viewType(viewBindingClass)
    }

    override fun onAttachedToRecyclerView(view: RecyclerView) {
        super.onAttachedToRecyclerView(view)
        ctx = view.context
        recycler = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TTHolder<*> {
        return TTHolder(inflateMethod.invoke(null, LayoutInflater.from(ctx), parent, false) as B)
    }

    override fun onViewRecycled(holder: TTHolder<*>) {
        super.onViewRecycled(holder)
        holder.disposables.disposeAll()
    }

    protected fun setClickListener(
        holder: TTHolder<B>, item: T?, listener: TTOnClickListener<B, T>, vararg views: View
    ) {
        val l = View.OnClickListener { v -> listener.onClick(v, holder, item) }
        for (v in views) v.setOnClickListener(l)
    }
}