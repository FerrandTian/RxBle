package tk.limt.demo

import android.bluetooth.BluetoothGattService
import android.view.View
import tk.limt.demo.databinding.ItemServiceBinding
import tk.limt.rxble.GattAttributes
import tk.limt.rxble.RxBle

class ServiceAdapter(
    val ble: RxBle
) : Adapter<ItemServiceBinding, BluetoothGattService>() {

    override fun onBindViewHolder(holder: Holder<ItemServiceBinding>, position: Int) {
        val item = items[position]
        holder.vb.tvName.text = GattAttributes.lookup(
            item.uuid.toString(), context.getString(R.string.unknown_service)
        )
        holder.vb.tvUuid.text = item.uuid.toString()
        holder.vb.tvType.setText(if (item.type == BluetoothGattService.SERVICE_TYPE_PRIMARY) R.string.primary_service else R.string.secondary_service)
        holder.vb.characteristics.removeAllViews()
        holder.vb.characteristics.visibility = View.GONE
        if (item.characteristics.isNotEmpty()) {
            holder.vb.characteristics.adapter = CharacteristicAdapter(ble, item.characteristics)
            gone(holder.vb.tvEmpty)
            visible(holder.vb.characteristics)
        } else {
            gone(holder.vb.characteristics)
            visible(holder.vb.tvEmpty)
        }
        holder.vb.service.setOnClickListener {
            holder.vb.characteristics.visibility = if (
                holder.vb.characteristics.isShown
            ) View.GONE else View.VISIBLE
        }
    }
}