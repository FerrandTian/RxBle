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

package tk.limt.demo.adapter

import android.bluetooth.le.ScanResult
import android.os.Build
import android.view.View
import androidx.viewbinding.ViewBinding
import tk.limt.demo.databinding.ItemScanBinding
import tk.limt.demo.displayName
import tt.tt.component.TTAdapter
import tt.tt.component.TTHolder
import tt.tt.component.TTOnClickListener

class ScanAdapter(
    private var clickListener: TTOnClickListener<ItemScanBinding, ScanResult>
) : TTAdapter<ItemScanBinding, ScanResult>() {

    var keyword: String? = null
        set(value) {
            if (value == field) return
            if (value != null && value.isNotEmpty()) {
                list = ArrayList()
                for (i in items.indices) {
                    val name = items[i].displayName
                    if (name?.isNotEmpty() == true && name.contains(value, true)) list.add(items[i])
                }
            } else list = items
            field = value
            notifyDataSetChanged()
        }
    var list: MutableList<ScanResult> = items

    override fun onBindViewHolder(holder: TTHolder<ViewBinding>, position: Int) {
        if (holder.vb is ItemScanBinding) {
            val vb = holder.vb as ItemScanBinding
            val item = list[position]
            item.device?.let {
                vb.tvAddress.text = it.address
            }
            vb.tvName.text = item.displayName ?: "N/A"
            vb.tvRssi.text = "${item.rssi} dBm"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vb.connect.visibility = if (item.isConnectable) View.VISIBLE else View.GONE
            }
            setClickListener(
                clickListener, holder as TTHolder<ItemScanBinding>, item,
                holder.itemView, vb.connect
            )
        }
    }

    override fun put(t: ScanResult): Boolean {
        val index = items.deviceIndexOf(t)
        if (index >= 0) items[index] = t else items.add(t)
        if (keyword?.isNotEmpty() == true) {
            keyword?.let { key ->
                val name = t.displayName
                if (name?.isNotEmpty() == true && name.contains(key, true)) {
                    val position = list.deviceIndexOf(t)
                    if (position >= 0) {
                        list[position] = t
                        notifyItemChanged(position)
                    } else {
                        list.add(t)
                        notifyItemInserted(itemCount - 1)
                    }
                }
            }
        } else {
            if (index >= 0) notifyItemChanged(index) else notifyItemInserted(list.size - 1)
        }
        return true
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun List<ScanResult>.deviceIndexOf(element: ScanResult): Int {
        for (i in indices) if (element.device == get(i).device) return i
        return -1
    }

    override fun clear() {
        val itemCount = list.size
        list.clear()
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
    }
}