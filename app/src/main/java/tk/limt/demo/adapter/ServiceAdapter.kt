package tk.limt.demo.adapter

import android.bluetooth.BluetoothGattService
import android.view.View
import tk.limt.demo.R
import tk.limt.demo.databinding.ItemServiceBinding
import tk.limt.rxble.GattAttributes
import tk.limt.rxble.RxBle
import tt.tt.component.TTAdapter
import tt.tt.component.TTHolder
import tt.tt.utils.gone
import tt.tt.utils.visible

class ServiceAdapter(
    val ble: RxBle
) : TTAdapter<ItemServiceBinding, BluetoothGattService>() {

    override fun onBindViewHolder(holder: TTHolder<ItemServiceBinding>, position: Int) {
        val item = items[position]
        holder.vb.tvName.text = GattAttributes.lookup(
            item.uuid.toString(), ctx.getString(R.string.unknown_service)
        )
        holder.vb.tvUuid.text = item.uuid.toString()
        holder.vb.tvType.setText(if (item.type == BluetoothGattService.SERVICE_TYPE_PRIMARY) R.string.primary_service else R.string.secondary_service)
        if (item.characteristics.isNotEmpty()) {
            holder.vb.recycler.adapter = CharacteristicAdapter(ble, item.characteristics)
            gone(holder.vb.tvEmpty)
            visible(holder.vb.recycler)
        } else {
            gone(holder.vb.recycler)
            visible(holder.vb.tvEmpty)
        }
        holder.vb.service.setOnClickListener {
            holder.vb.llCharacteristics.visibility = if (
                holder.vb.llCharacteristics.isShown
            ) View.GONE else View.VISIBLE
        }
    }
}