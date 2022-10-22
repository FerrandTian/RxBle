package tk.limt.demo

import android.bluetooth.BluetoothGattDescriptor
import android.view.View
import android.widget.Toast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import tk.limt.demo.databinding.ItemDescriptorBinding
import tk.limt.rxble.GattAttributes
import tk.limt.rxble.RxBle
import tk.limt.utils.hex

class DescriptorAdapter(
    val ble: RxBle,
    items: MutableList<BluetoothGattDescriptor>
) : Adapter<ItemDescriptorBinding, BluetoothGattDescriptor>(items) {
    init {
        clickListener = object : OnItemClickListener<
                ItemDescriptorBinding, BluetoothGattDescriptor> {
            override fun onItemClick(
                view: View, holder: Holder<ItemDescriptorBinding>, item: BluetoothGattDescriptor
            ) {
                ble.read(item).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    holder.vb.tvValue.text = it.value.hex()
                    visible(holder.vb.tvValueTitle, holder.vb.tvValue)
                }) { Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show() }
            }
        }
    }

    override fun onBindViewHolder(holder: Holder<ItemDescriptorBinding>, position: Int) {
        val item = items[position]
        holder.vb.tvName.text = GattAttributes.lookup(
            item.uuid.toString(), context.getString(R.string.unknown_descriptor)
        )
        holder.vb.tvUuid.text = item.uuid.toString()
        item.value?.let {
            holder.vb.tvValue.text = it.hex()
            visible(holder.vb.tvValueTitle, holder.vb.tvValue)
        } ?: {
            gone(holder.vb.tvValueTitle, holder.vb.tvValue)
        }
        setClickListener(holder, item, holder.vb.ivRead)
    }
}

