package tk.limt.demo.adapter

import android.bluetooth.BluetoothGattDescriptor
import android.view.View
import android.widget.Toast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import tk.limt.demo.R
import tk.limt.demo.databinding.ItemDescriptorBinding
import tk.limt.rxble.GattAttributes
import tk.limt.rxble.RxBle
import tt.tt.component.TTAdapter
import tt.tt.component.TTHolder
import tt.tt.component.TTItemClickListener
import tt.tt.utils.gone
import tt.tt.utils.hex
import tt.tt.utils.visible

class DescriptorAdapter(
    val ble: RxBle,
    items: MutableList<BluetoothGattDescriptor>
) : TTAdapter<ItemDescriptorBinding, BluetoothGattDescriptor>(items) {
    init {
        clickListener = object : TTItemClickListener<
                ItemDescriptorBinding, BluetoothGattDescriptor> {
            override fun onItemClick(
                view: View, holder: TTHolder<ItemDescriptorBinding>, item: BluetoothGattDescriptor
            ) {
                ble.read(item).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    holder.vb.tvValue.text = it.value.hex(true)
                    visible(holder.vb.tvValueTitle, holder.vb.tvValue)
                }) { Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show() }
            }
        }
    }

    override fun onBindViewHolder(holder: TTHolder<ItemDescriptorBinding>, position: Int) {
        val item = items[position]
        holder.vb.tvName.text = GattAttributes.lookup(
            item.uuid.toString(), context.getString(R.string.unknown_descriptor)
        )
        holder.vb.tvUuid.text = item.uuid.toString()
        item.value?.let {
            holder.vb.tvValue.text = it.hex(true)
            visible(holder.vb.tvValueTitle, holder.vb.tvValue)
        } ?: run { gone(holder.vb.tvValueTitle, holder.vb.tvValue) }
        setClickListener(holder, item, holder.vb.ivRead)
    }
}