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

import android.bluetooth.BluetoothGattService
import android.view.View
import androidx.viewbinding.ViewBinding
import tk.limt.demo.R
import tk.limt.demo.databinding.ItemServiceBinding
import tk.limt.rxble.GattAttributes
import tt.tt.component.TTAdapter
import tt.tt.component.TTHolder
import tt.tt.component.TTOnClickListener
import tt.tt.utils.gone
import tt.tt.utils.visible

class ServiceAdapter(
    val address: String
) : TTAdapter<ItemServiceBinding, BluetoothGattService>() {
    private val clickListener = object :
        TTOnClickListener<ItemServiceBinding, BluetoothGattService> {
        override fun onClick(
            view: View,
            holder: TTHolder<ItemServiceBinding>,
            t: BluetoothGattService?
        ) {
            holder.vb.llCharacteristics.visibility = if (
                holder.vb.llCharacteristics.isShown
            ) View.GONE else View.VISIBLE
        }
    }

    override fun onBindViewHolder(holder: TTHolder<ViewBinding>, position: Int) {
        if (holder.vb is ItemServiceBinding) {
            val vb = holder.vb as ItemServiceBinding
            val item = items[position]
            vb.tvName.text = GattAttributes.lookup(
                item.uuid.toString(), ctx.getString(R.string.unknown_service)
            )
            vb.tvUuid.text = item.uuid.toString()
            vb.tvType.setText(if (item.type == BluetoothGattService.SERVICE_TYPE_PRIMARY) R.string.primary_service else R.string.secondary_service)
            if (item.characteristics.isNotEmpty()) {
                vb.recycler.adapter = CharacteristicAdapter(address, item.characteristics)
                gone(vb.tvEmpty)
                visible(vb.recycler)
            } else {
                gone(vb.recycler)
                visible(vb.tvEmpty)
            }
            setClickListener(
                clickListener, holder as TTHolder<ItemServiceBinding>, item, vb.service
            )
        }
    }
}