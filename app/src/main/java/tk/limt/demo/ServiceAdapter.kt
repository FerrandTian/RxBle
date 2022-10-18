package tk.limt.demo

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.view.LayoutInflater
import android.view.View
import tk.limt.demo.databinding.ItemCharacteristicBinding
import tk.limt.demo.databinding.ItemDescriptorBinding
import tk.limt.demo.databinding.ItemEmptyBinding
import tk.limt.demo.databinding.ItemServiceBinding
import tk.limt.rxble.GattAttributes

class ServiceAdapter(
    clickListener: OnItemClickListener<ItemServiceBinding, BluetoothGattService>? = null
) : Adapter<ItemServiceBinding, BluetoothGattService>(clickListener = clickListener) {

    override fun onBindViewHolder(holder: Holder<ItemServiceBinding>, position: Int) {
        val item = items[position]
        holder.binding.name.text = GattAttributes.lookup(
            item.uuid.toString(), context.getString(R.string.unknown_service)
        )
        holder.binding.uuid.text = item.uuid.toString()
        holder.binding.type.setText(if (item.type == BluetoothGattService.SERVICE_TYPE_PRIMARY) R.string.primary_service else R.string.secondary_service)
        holder.binding.characteristics.removeAllViews()
        if (item.characteristics.isNotEmpty()) {
            item.characteristics.forEach { ch ->
                val chBinding = ItemCharacteristicBinding.inflate(
                    LayoutInflater.from(context), holder.binding.characteristics, true
                )
                chBinding.name.text = GattAttributes.lookup(
                    ch.uuid.toString(), context.getString(R.string.unknown_characteristic)
                )
                chBinding.uuid.text = ch.uuid.toString()
                chBinding.props.text = ch.propertiesString
                if (ch.descriptors.isNotEmpty()) {
                    ch.descriptors.forEach { desc ->
                        val descBinding = ItemDescriptorBinding.inflate(
                            LayoutInflater.from(context), chBinding.descriptors, true
                        )
                        descBinding.name.text = GattAttributes.lookup(
                            desc.uuid.toString(), context.getString(R.string.unknown_descriptor)
                        )
                        descBinding.uuid.text = desc.uuid.toString()
                    }
                    chBinding.tvDescriptors.visibility = View.VISIBLE
                    chBinding.descriptors.visibility = View.VISIBLE
                } else {
                    chBinding.tvDescriptors.visibility = View.GONE
                    chBinding.descriptors.visibility = View.GONE
                }
            }
        } else {
            val emptyBinding = ItemEmptyBinding.inflate(
                LayoutInflater.from(context), holder.binding.characteristics, true
            )
            emptyBinding.empty.setText(R.string.empty_service)
        }
        holder.binding.service.setOnClickListener {
            holder.binding.characteristics.visibility = if (
                holder.binding.characteristics.isShown
            ) View.GONE else View.VISIBLE
        }
//        setClickListener(holder, item, holder.itemView)
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
                    builder.append(context.getString(it.value))
                    count++
                }
            }
            return builder.toString()
        }
}