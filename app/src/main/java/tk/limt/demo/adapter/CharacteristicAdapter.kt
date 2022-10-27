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

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import tk.limt.demo.R
import tk.limt.demo.databinding.ItemCharacteristicBinding
import tk.limt.rxble.GattAttributes
import tk.limt.rxble.RxBle
import tk.limt.utils.notificationEnabled
import tk.limt.utils.support
import tk.limt.utils.supportNotification
import tt.tt.component.TTAdapter
import tt.tt.component.TTHolder
import tt.tt.component.TTItemClickListener
import tt.tt.rx.TTCompletableObserver
import tt.tt.rx.TTObserver
import tt.tt.rx.TTSingleObserver
import tt.tt.utils.*

class CharacteristicAdapter(
    val ble: RxBle,
    items: MutableList<BluetoothGattCharacteristic>
) : TTAdapter<ItemCharacteristicBinding, BluetoothGattCharacteristic>(items) {
    init {
        clickListener = object :
            TTItemClickListener<ItemCharacteristicBinding, BluetoothGattCharacteristic> {
            override fun onItemClick(
                view: View,
                holder: TTHolder<ItemCharacteristicBinding>,
                item: BluetoothGattCharacteristic
            ) {
                when (view) {
                    holder.vb.ivRead -> ble.read(item).observeOn(
                        AndroidSchedulers.mainThread()
                    ).subscribe(object :
                        TTSingleObserver<BluetoothGattCharacteristic>(holder.disposables) {
                        override fun onSuccess(t: BluetoothGattCharacteristic) {
                            super.onSuccess(t)
                            holder.vb.tvValue.text = t.value.hex(true)
                            visible(holder.vb.tvValueTitle, holder.vb.tvValue)
                        }

                        override fun onError(e: Throwable) {
                            super.onError(e)
                            e.message?.let { it -> ctx.toast(it) }
                        }
                    })
                    holder.vb.ivWrite -> showSendDialog(holder, item)
                    holder.vb.ivNotify -> {
                        item.descriptors[0].value = if (
                            item.notificationEnabled
                        ) BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        ble.setNotification(item.descriptors[0]).observeOn(
                            AndroidSchedulers.mainThread()
                        ).subscribe(object :
                            TTSingleObserver<BluetoothGattDescriptor>(holder.disposables) {
                            override fun onSuccess(t: BluetoothGattDescriptor) {
                                super.onSuccess(t)
                                holder.vb.ivNotify.setImageResource(
                                    if (
                                        item.notificationEnabled
                                    ) R.drawable.ic_download_multiple_disable_24 else R.drawable.ic_download_multiple_24
                                )
                                holder.vb.descriptors.adapter?.notifyItemChanged(0)
                            }

                            override fun onError(e: Throwable) {
                                super.onError(e)
                                e.message?.let { it -> ctx.toast(it) }
                            }
                        })
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: TTHolder<ItemCharacteristicBinding>, position: Int) {
        val item = items[position]
        holder.vb.tvName.text = GattAttributes.lookup(
            item.uuid.toString(), ctx.getString(R.string.unknown_characteristic)
        )
        holder.vb.tvUuid.text = item.uuid.toString()
        holder.vb.tvProps.text = item.propertiesString
        gone(item.support(BluetoothGattCharacteristic.PROPERTY_READ), holder.vb.ivRead)
        gone(item.support(BluetoothGattCharacteristic.PROPERTY_WRITE), holder.vb.ivWrite)
        if (item.supportNotification) {
            holder.vb.ivNotify.setImageResource(
                if (
                    item.notificationEnabled
                ) R.drawable.ic_download_multiple_disable_24 else R.drawable.ic_download_multiple_24
            )
            holder.vb.descriptors.adapter = DescriptorAdapter(ble, item.descriptors)
            visible(holder.vb.ivNotify, holder.vb.tvDescriptorsTitle, holder.vb.descriptors)
        } else {
            gone(holder.vb.ivNotify, holder.vb.tvDescriptorsTitle, holder.vb.descriptors)
        }
        ble.characteristic(item.uuid).observeOn(
            AndroidSchedulers.mainThread()
        ).subscribe(object : TTObserver<BluetoothGattCharacteristic>(holder.disposables) {
            override fun onNext(t: BluetoothGattCharacteristic) {
                holder.vb.tvValue.text = t.value.hex(true)
                visible(holder.vb.tvValueTitle, holder.vb.tvValue)
            }
        })
        setClickListener(holder, item, holder.vb.ivRead, holder.vb.ivWrite, holder.vb.ivNotify)
    }

    fun showSendDialog(
        holder: TTHolder<ItemCharacteristicBinding>,
        characteristic: BluetoothGattCharacteristic
    ) {
        AlertDialog.Builder(ctx).setTitle(R.string.write_value).setView(
            R.layout.dialog_send
        ).setPositiveButton(
            tt.tt.R.string.tt_send
        ) { dialog, which ->
            val text = (dialog as AlertDialog).findViewById<EditText>(R.id.et_value)?.text?.trim()
            text?.hexToBytes()?.let {
                if (it.size > ble.mtu) {
                    ble.write(it.split(ble.mtu).asIterable(), characteristic).observeOn(
                        AndroidSchedulers.mainThread()
                    ).subscribe(object : TTCompletableObserver(holder.disposables) {
                        override fun onComplete() {
                            super.onComplete()
                            ctx.toast(tt.tt.R.string.tt_complete)
                        }

                        override fun onError(e: Throwable) {
                            super.onError(e)
                            e.message?.let { it -> ctx.toast(it) }
                        }
                    })
                } else {
                    ble.write(characteristic).observeOn(
                        AndroidSchedulers.mainThread()
                    ).subscribe(object :
                        TTSingleObserver<BluetoothGattCharacteristic>(holder.disposables) {
                        override fun onSubscribe(d: Disposable) {
                            super.onSubscribe(d)
                            characteristic.value = it
                        }

                        override fun onSuccess(t: BluetoothGattCharacteristic) {
                            super.onSuccess(t)
                            holder.vb.tvValue.text = t.value.hex(true)
                            visible(holder.vb.tvValueTitle, holder.vb.tvValue)
                        }

                        override fun onError(e: Throwable) {
                            super.onError(e)
                            e.message?.let { it -> ctx.toast(it) }
                        }
                    })
                }
            }
        }.setNegativeButton(tt.tt.R.string.tt_cancel, null).show()
    }

    private val propertiesMap = mapOf(
        BluetoothGattCharacteristic.PROPERTY_BROADCAST to R.string.broadcast,
        BluetoothGattCharacteristic.PROPERTY_READ to R.string.read,
        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE to R.string.write_no_response,
        BluetoothGattCharacteristic.PROPERTY_WRITE to R.string.write,
        BluetoothGattCharacteristic.PROPERTY_NOTIFY to R.string.notify,
        BluetoothGattCharacteristic.PROPERTY_INDICATE to R.string.indicate,
        BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE to R.string.signed_write,
        BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS to R.string.extended_props
    )
    val BluetoothGattCharacteristic.propertiesString: String
        get() {
            val builder = StringBuilder()
            var count = 0
            propertiesMap.forEach {
                if (properties and it.key != 0) {
                    if (count > 0) builder.append(", ")
                    builder.append(ctx.getString(it.value))
                    count++
                }
            }
            return builder.toString()
        }
}