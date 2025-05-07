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

package tt.rxble.adapter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import tt.rxble.databinding.ItemListTitleBinding
import tt.rxble.databinding.ItemScanBinding
import tt.rxble.displayName
import tt.rxble.R
import tt.base.component.TTAdapter
import tt.base.component.TTHolder
import tt.base.component.TTOnClickListener
import tt.base.utils.gone
import tt.base.utils.visible

class ScanAdapter(
    private var clickListener: TTOnClickListener<ItemScanBinding, BluetoothDevice>
) : TTAdapter<ItemScanBinding, ScanResult>() {
    var bondedDevices = mutableListOf<BluetoothDevice>()

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

    override fun getItemViewType(position: Int): Int {
        if (bondedDevices.isNotEmpty() && (position == 0 || position == (bondedDevices.size + 1))) {
            return TTHolder.viewType(ItemListTitleBinding::class.java)
        }
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TTHolder<ViewBinding> {
        if (viewType == TTHolder.viewType(ItemListTitleBinding::class.java)) {
            return TTHolder(ItemListTitleBinding.inflate(LayoutInflater.from(ctx), parent, false))
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: TTHolder<ViewBinding>, position: Int) {
        val resources = holder.itemView.resources
        if (holder.vb is ItemScanBinding) {
            val vb = holder.vb as ItemScanBinding
            if (bondedDevices.isNotEmpty() && position < (bondedDevices.size + 2)) {
                val item = bondedDevices[position - 1]
                vb.tvAddress.text = item.address
                var name = item.name
                if (name.isNullOrBlank() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    name = item.alias
                }
                if (name.isNullOrBlank()) name = resources.getString(R.string.n_a)
                vb.tvName.text = name
                gone(vb.tvRssi)
                setClickListener(
                    clickListener, holder as TTHolder<ItemScanBinding>, item,
                    holder.itemView, vb.connect
                )
            } else {
                var pos = getRealPosition(position, true)
                val item = list[pos]
                item.device?.let {
                    vb.tvAddress.text = it.address
                }
                vb.tvName.text = item.displayName ?: resources.getString(R.string.n_a)
                vb.tvRssi.text = resources.getString(R.string.dbm, item.rssi)
                visible(vb.tvRssi)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vb.connect.visibility = if (item.isConnectable) View.VISIBLE else View.GONE
                }
                setClickListener(
                    clickListener, holder as TTHolder<ItemScanBinding>, item.device,
                    holder.itemView, vb.connect
                )
            }
        } else if (holder.vb is ItemListTitleBinding) {
            val vb = holder.vb as ItemListTitleBinding
            if (position == 0) {
                vb.tvTitle.text = resources.getString(R.string.bonded_devices)
            } else {
                vb.tvTitle.text = resources.getString(R.string.available_devices)
            }
        }
    }

    override fun put(t: ScanResult): Boolean {
        var index = items.deviceIndexOf(t)
        if (index >= 0) items[index] = t else items.add(t)
        if (keyword?.isNotEmpty() == true) {
            keyword?.let { key ->
                val name = t.displayName
                if (name?.isNotEmpty() == true && name.contains(key, true)) {
                    val position = list.deviceIndexOf(t)
                    if (position >= 0) {
                        list[position] = t
                        notifyItemChanged(getRealPosition(position))
                    } else {
                        list.add(t)
                        notifyItemInserted(itemCount - 1)
                    }
                }
            }
        } else {
            if (index >= 0) {
                notifyItemChanged(getRealPosition(index))
            } else notifyItemInserted(itemCount - 1)
        }
        return true
    }

    override fun getItemCount() = getRealPosition(list.size)

    private fun getRealPosition(position: Int, reverse: Boolean = false): Int {
        if (bondedDevices.isNotEmpty()) {
            return if (reverse) position - bondedDevices.size - 2 else position + bondedDevices.size + 2
        }
        return position
    }

    private fun List<ScanResult>.deviceIndexOf(element: ScanResult): Int {
        for (i in indices) if (element.device == get(i).device) return i
        return -1
    }

    override fun clear() {
        val count = itemCount
        bondedDevices.clear()
        list.clear()
        items.clear()
        notifyItemRangeRemoved(0, count)
    }
}