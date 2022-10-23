package tk.limt.demo.adapter

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.view.View
import android.widget.Toast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
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
import tt.tt.utils.gone
import tt.tt.utils.hex
import tt.tt.utils.visible

class CharacteristicAdapter(
    val ble: RxBle,
    items: MutableList<BluetoothGattCharacteristic>
) : TTAdapter<ItemCharacteristicBinding, BluetoothGattCharacteristic>(items) {
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

    init {
        clickListener = object : TTItemClickListener<
                ItemCharacteristicBinding, BluetoothGattCharacteristic> {
            override fun onItemClick(
                view: View,
                holder: TTHolder<ItemCharacteristicBinding>,
                item: BluetoothGattCharacteristic
            ) {
                when (view) {
                    holder.vb.ivRead -> {
                        ble.read(item).observeOn(AndroidSchedulers.mainThread()).subscribe({
                            holder.vb.tvValue.text = it.value.hex(true)
                            visible(holder.vb.tvValueTitle, holder.vb.tvValue)
                        }) { Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show() }
                    }
                    holder.vb.ivNotify -> {
                        item.descriptors[0].value = if (
                            item.notificationEnabled
                        ) BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        ble.setNotification(item.descriptors[0]).observeOn(
                            AndroidSchedulers.mainThread()
                        ).subscribe({
                            holder.vb.ivNotify.setImageResource(
                                if (
                                    item.notificationEnabled
                                ) R.drawable.ic_download_multiple_disable_24 else R.drawable.ic_download_multiple_24
                            )
                            holder.vb.descriptors.adapter?.notifyItemChanged(0)
                        }) { Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show() }
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: TTHolder<ItemCharacteristicBinding>, position: Int) {
        val item = items[position]
        holder.vb.tvName.text = GattAttributes.lookup(
            item.uuid.toString(), context.getString(R.string.unknown_characteristic)
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
        setClickListener(holder, item, holder.vb.ivRead, holder.vb.ivWrite, holder.vb.ivNotify)
    }

    val BluetoothGattCharacteristic.propertiesString: String
        get() {
            val builder = StringBuilder()
            var count = 0
            propertiesMap.forEach {
                if (properties and it.key != 0) {
                    if (count > 0) builder.append(", ")
                    builder.append(context.getString(it.value))
                    count++
                }
            }
            return builder.toString()
        }
}