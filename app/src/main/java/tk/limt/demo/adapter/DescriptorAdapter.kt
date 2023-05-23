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

import android.bluetooth.BluetoothGattDescriptor
import android.view.View
import androidx.viewbinding.ViewBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import tk.limt.demo.R
import tk.limt.demo.data.DeviceManager
import tk.limt.demo.databinding.ItemDescriptorBinding
import tk.limt.rxble.GattAttributes
import tk.limt.utils.hex
import tt.tt.component.TTAdapter
import tt.tt.component.TTHolder
import tt.tt.component.TTOnClickListener
import tt.tt.rx.TTSingleObserver
import tt.tt.utils.gone
import tt.tt.utils.toast
import tt.tt.utils.visible

class DescriptorAdapter(
    val address: String,
    items: MutableList<BluetoothGattDescriptor>,
) : TTAdapter<ItemDescriptorBinding, BluetoothGattDescriptor>(items) {
    private val manager = DeviceManager.instance
    private val clickListener = object :
        TTOnClickListener<ItemDescriptorBinding, BluetoothGattDescriptor> {
        override fun onClick(
            v: View, h: TTHolder<ItemDescriptorBinding>, t: BluetoothGattDescriptor?,
        ) {
            t?.let {
                manager.obtain(address).read(it).observeOn(
                    AndroidSchedulers.mainThread()
                ).subscribe(object : TTSingleObserver<ByteArray>(h.disposables) {
                    override fun onSuccess(t: ByteArray) {
                        super.onSuccess(t)
                        h.vb.tvValue.text = t.hex(true)
                        visible(h.vb.tvValueTitle, h.vb.tvValue)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        e.message?.let { it -> ctx.toast(it) }
                    }
                })
            }
        }
    }

    override fun onBindViewHolder(holder: TTHolder<ViewBinding>, position: Int) {
        if (holder.vb is ItemDescriptorBinding) {
            val vb = holder.vb as ItemDescriptorBinding
            val item = items[position]
            vb.tvName.text = GattAttributes.lookup(
                item.uuid.toString(), ctx.getString(R.string.unknown_descriptor)
            )
            vb.tvUuid.text = item.uuid.toString()
            item.value?.let {
                vb.tvValue.text = it.hex(true)
                visible(vb.tvValueTitle, vb.tvValue)
            } ?: gone(vb.tvValueTitle, vb.tvValue)
            setClickListener(
                clickListener, holder as TTHolder<ItemDescriptorBinding>, item, vb.ivRead
            )
        }
    }
}