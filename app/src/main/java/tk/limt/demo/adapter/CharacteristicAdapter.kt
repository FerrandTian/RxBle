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
import androidx.viewbinding.ViewBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import tk.limt.demo.R
import tk.limt.demo.data.DeviceManager
import tk.limt.demo.databinding.ItemCharacteristicBinding
import tk.limt.rxble.GattAttributes
import tk.limt.utils.*
import tt.tt.component.TTAdapter
import tt.tt.component.TTHolder
import tt.tt.component.TTOnClickListener
import tt.tt.rx.TTCompletableObserver
import tt.tt.rx.TTObserver
import tt.tt.rx.TTSingleObserver
import tt.tt.utils.gone
import tt.tt.utils.toast
import tt.tt.utils.visible

class CharacteristicAdapter(
    val address: String,
    items: MutableList<BluetoothGattCharacteristic>,
) : TTAdapter<ItemCharacteristicBinding, BluetoothGattCharacteristic>(items) {
    private val manager = DeviceManager.instance
    private val clickListener = object :
        TTOnClickListener<ItemCharacteristicBinding, BluetoothGattCharacteristic> {
        override fun onClick(
            v: View, h: TTHolder<ItemCharacteristicBinding>, ch: BluetoothGattCharacteristic?,
        ) {
            when (v) {
                h.vb.ivRead -> ch?.let {
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
                h.vb.ivWrite -> ch?.let { showSendDialog(h, it) }
                h.vb.ivNotify -> {
                    ch?.let {
                        manager.obtain(address).setNotification(
                            ch.descriptors[0], if (
                                ch.isNotificationEnabled
                            ) BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        ).observeOn(
                            AndroidSchedulers.mainThread()
                        ).subscribe(object : TTCompletableObserver(h.disposables) {
                            override fun onComplete() {
                                super.onComplete()
                                h.vb.ivNotify.setImageResource(
                                    if (ch.isNotificationEnabled) {
                                        R.drawable.ic_download_multiple_disable_24
                                    } else R.drawable.ic_download_multiple_24
                                )
                                h.vb.descriptors.adapter?.notifyItemChanged(0)
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

    override fun onBindViewHolder(holder: TTHolder<ViewBinding>, position: Int) {
        if (holder.vb is ItemCharacteristicBinding) {
            val vb = holder.vb as ItemCharacteristicBinding
            val item = items[position]
            vb.tvName.text = GattAttributes.lookup(
                item.uuid.toString(), ctx.getString(R.string.unknown_characteristic)
            )
            vb.tvUuid.text = item.uuid.toString()
            vb.tvProps.text = item.propertiesString
            gone(
                item.isPropertySupported(BluetoothGattCharacteristic.PROPERTY_READ),
                vb.ivRead
            )
            gone(
                item.isPropertySupported(BluetoothGattCharacteristic.PROPERTY_WRITE),
                vb.ivWrite
            )
            if (item.isNotificationSupported) {
                vb.ivNotify.setImageResource(
                    if (
                        item.isNotificationEnabled
                    ) R.drawable.ic_download_multiple_disable_24 else R.drawable.ic_download_multiple_24
                )
                vb.descriptors.adapter = DescriptorAdapter(address, item.descriptors)
                visible(vb.ivNotify, vb.tvDescriptorsTitle, vb.descriptors)
            } else {
                gone(vb.ivNotify, vb.tvDescriptorsTitle, vb.descriptors)
            }
            manager.obtain(address).characteristic(item.uuid).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe(object : TTObserver<ByteArray>(holder.disposables) {
                override fun onNext(t: ByteArray) {
                    vb.tvValue.text = t.hex(true)
                    visible(vb.tvValueTitle, vb.tvValue)
                }
            })
            setClickListener(
                clickListener, holder as TTHolder<ItemCharacteristicBinding>, item,
                vb.ivRead, vb.ivWrite, vb.ivNotify
            )
        }
    }

    fun showSendDialog(
        holder: TTHolder<ItemCharacteristicBinding>,
        characteristic: BluetoothGattCharacteristic,
    ) {
        AlertDialog.Builder(ctx).setTitle(R.string.write_value).setView(
            R.layout.dialog_send
        ).setPositiveButton(tt.tt.R.string.tt_send) { dialog, which ->
            val text = (dialog as AlertDialog).findViewById<EditText>(R.id.et_value)?.text?.trim()
            text?.hexToBytes()?.let {
                ((if (it.size > manager.obtain(address).mtu) manager.obtain(address).writeWithQueue(
                    characteristic, it
                ) else manager.obtain(address).write(characteristic, it))).observeOn(
                    AndroidSchedulers.mainThread()
                ).subscribe(object : TTCompletableObserver(holder.disposables) {
                    override fun onComplete() {
                        super.onComplete()
                        holder.vb.tvValue.text = it.hex(true)
                        visible(holder.vb.tvValueTitle, holder.vb.tvValue)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        e.message?.let { it -> ctx.toast(it) }
                    }
                })
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