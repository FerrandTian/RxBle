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
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

/**
 * @author tianfeng
 */
abstract class TTAdapter<B : ViewBinding, T>(
    @JvmField val items: MutableList<T> = ArrayList(),
) : RecyclerView.Adapter<TTHolder<ViewBinding>>() {
    private val parameterizedType = javaClass.genericSuperclass as ParameterizedType
    private val viewBindingClass = parameterizedType.actualTypeArguments[0] as Class<B>
    private val inflate = viewBindingClass.getDeclaredMethod(
        "inflate", LayoutInflater::class.java,
        ViewGroup::class.java, Boolean::class.javaPrimitiveType
    )
    lateinit var recycler: RecyclerView
    lateinit var ctx: Context

    @JvmField
    var tracker: SelectionTracker<Long>? = null

    @JvmField
    val observer = object : SelectionTracker.SelectionObserver<Long>() {
        var state = false
        override fun onItemStateChanged(key: Long, selected: Boolean) {}

        override fun onSelectionRefresh() {}

        override fun onSelectionChanged() {
            val newState = hasSelection
            if (newState != state) {
                state = newState
                recycler.post { notifyDataSetChanged() }
            }
        }

        override fun onSelectionRestored() {}
    }

    @get:JvmName("hasSelection")
    val hasSelection: Boolean
        get() = tracker?.hasSelection() == true

    @get:JvmName("selectionSize")
    val selectionSize: Int
        get() = tracker?.selection?.size() ?: 0

    @get:JvmName("selection")
    val selection: Iterable<T>
        get() = tracker?.selection?.map {
            items[it.toInt()]
        } ?: ArrayList()

    @JvmField
    val itemKeyProvider = object : ItemKeyProvider<Long>(SCOPE_MAPPED) {
        override fun getKey(position: Int): Long {
            return getItemId(position)
        }

        override fun getPosition(key: Long): Int {
            return recycler.findViewHolderForItemId(
                key
            )?.bindingAdapterPosition ?: RecyclerView.NO_POSITION
        }
    }

    @JvmField
    val itemDetailsLookup = object : ItemDetailsLookup<Long>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
            return recycler.findChildViewUnder(event.x, event.y)?.let {
                (recycler.getChildViewHolder(it) as TTHolder<*>).itemDetails
            }
        }
    }

    init {
        this.setHasStableIds(true)
    }

    open fun getItem(position: Int): T? {
        return if (
            items.size > 0 && position >= 0 && position < items.size
        ) items[position] else null
    }

    open fun getItem(key: Long): T? {
        return getItem(key.toInt())
    }

    open fun isSelected(key: Long?): Boolean {
        return tracker?.isSelected(key) == true
    }

    open fun enableSelection(
        predicates: SelectionTracker.SelectionPredicate<Long>,
        obs: SelectionTracker.SelectionObserver<Long>,
    ) {
        SelectionTracker.Builder(
            this.javaClass.name, recycler, itemKeyProvider, itemDetailsLookup,
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(predicates).build().apply {
            this.addObserver(observer)
            this.addObserver(obs)
            tracker = this
        }
    }

    open fun clearSelection() {
        tracker?.clearSelection()
    }

    open fun setItems(ts: Collection<T>): Boolean {
        clearSelection()
        items.clear()
        return items.addAll(ts).also {
            notifyDataSetChanged()
        }
    }

    open fun add(t: T): Boolean {
        return items.add(t).also {
            if (it) notifyItemInserted(items.size - 1)
        }
    }

    open fun addAll(ts: Collection<T>): Boolean {
        val start = items.size
        return items.addAll(ts).also {
            if (it) notifyItemRangeInserted(start, ts.size)
        }
    }

    open fun update(t: T): T? {
        val i = items.indexOf(t)
        return if (i >= 0) items.set(i, t).also {
            notifyItemChanged(i)
        } else null
    }

    open fun put(t: T): Boolean {
        return if (update(t) == null) add(t) else false
    }

    open fun remove(position: Int): T? {
        return if (position >= 0 && position < items.size) items.removeAt(position).also {
            notifyItemRemoved(position)
        } else null
    }

    open fun remove(t: T): T? {
        val i = items.indexOf(t)
        return if (i >= 0) items.removeAt(i).also {
            notifyItemRemoved(i)
        } else null
    }

    open fun clear() {
        clearSelection()
        items.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return TTHolder.viewType(viewBindingClass)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onAttachedToRecyclerView(view: RecyclerView) {
        super.onAttachedToRecyclerView(view)
        recycler = view
        ctx = view.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TTHolder<ViewBinding> {
        return TTHolder(inflate.invoke(null, LayoutInflater.from(ctx), parent, false) as B)
    }

    override fun onViewRecycled(holder: TTHolder<ViewBinding>) {
        super.onViewRecycled(holder)
        holder.disposables.disposeAll()
    }

    companion object {
        @JvmStatic
        fun <B : ViewBinding, T> setClickListener(
            listener: TTOnClickListener<B, T>, holder: TTHolder<B>, t: T?, vararg views: View,
        ) {
            val l = View.OnClickListener { v -> listener.onClick(v, holder, t) }
            for (v in views) v.setOnClickListener(l)
        }

        @JvmStatic
        fun <B : ViewBinding, T> setLongClickListener(
            listener: TTOnLongClickListener<B, T>, holder: TTHolder<B>, t: T?, vararg views: View,
        ) {
            val l = View.OnLongClickListener { v -> listener.onLongClick(v, holder, t) }
            for (v in views) v.setOnLongClickListener(l)
        }
    }
}