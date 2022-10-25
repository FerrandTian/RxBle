package tk.limt.demo.adapter

import android.bluetooth.BluetoothGattDescriptor
import android.view.View
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import tk.limt.demo.R
import tk.limt.demo.databinding.ItemDescriptorBinding
import tk.limt.rxble.GattAttributes
import tk.limt.rxble.RxBle
import tt.tt.component.TTAdapter
import tt.tt.component.TTHolder
import tt.tt.component.TTItemClickListener
import tt.tt.rx.TTSingleObserver
import tt.tt.utils.gone
import tt.tt.utils.hex
import tt.tt.utils.toast
import tt.tt.utils.visible

class DescriptorAdapter(
    val ble: RxBle,
    items: MutableList<BluetoothGattDescriptor>
) : TTAdapter<ItemDescriptorBinding, BluetoothGattDescriptor>(items) {
    init {
        clickListener = object :
            TTItemClickListener<ItemDescriptorBinding, BluetoothGattDescriptor> {
            override fun onItemClick(
                view: View,
                holder: TTHolder<ItemDescriptorBinding>,
                item: BluetoothGattDescriptor
            ) {
                ble.read(item).observeOn(AndroidSchedulers.mainThread()).subscribe(
                    object : TTSingleObserver<BluetoothGattDescriptor>(holder.disposables) {
                        override fun onSuccess(t: BluetoothGattDescriptor) {
                            super.onSuccess(t)
                            holder.vb.tvValue.text = t.value.hex(true)
                            visible(holder.vb.tvValueTitle, holder.vb.tvValue)
                        }

                        override fun onError(e: Throwable) {
                            super.onError(e)
                            e.message?.let { it -> ctx.toast(it) }
                        }
                    }
                )
            }
        }
    }

    override fun onBindViewHolder(holder: TTHolder<ItemDescriptorBinding>, position: Int) {
        val item = items[position]
        holder.vb.tvName.text = GattAttributes.lookup(
            item.uuid.toString(), ctx.getString(R.string.unknown_descriptor)
        )
        holder.vb.tvUuid.text = item.uuid.toString()
        item.value?.let {
            holder.vb.tvValue.text = it.hex(true)
            visible(holder.vb.tvValueTitle, holder.vb.tvValue)
        } ?: run { gone(holder.vb.tvValueTitle, holder.vb.tvValue) }
        setClickListener(holder, item, holder.vb.ivRead)
    }
}