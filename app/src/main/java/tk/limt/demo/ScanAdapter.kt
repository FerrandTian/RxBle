package tk.limt.demo

import android.bluetooth.le.ScanResult
import android.os.Build
import android.view.View
import tk.limt.demo.databinding.ItemScanBinding

class ScanAdapter(
    clickListener: OnItemClickListener<ItemScanBinding, ScanResult>? = null
) : Adapter<ItemScanBinding, ScanResult>(clickListener = clickListener) {

    var keyword: String? = null
        set(value) {
            if (value == field) return
            if (value != null && value.isNotEmpty()) {
                list = ArrayList()
                for (i in items.indices) {
                    val name = items[i].deviceName
                    if (name?.isNotEmpty() == true && name.contains(value, true)) list.add(items[i])
                }
            } else list = items
            field = value
            notifyDataSetChanged()
        }
    var list: MutableList<ScanResult> = items

    override fun onBindViewHolder(holder: Holder<ItemScanBinding>, position: Int) {
        val item = list[position]
        item.device?.let {
            holder.binding.address.text = it.address
        }
        holder.binding.name.text = item.deviceName ?: "N/A"
        holder.binding.rssi.text = "${item.rssi} dBm"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.binding.connect.visibility = if (item.isConnectable) View.VISIBLE else View.GONE
        }
        setClickListener(holder, item, holder.itemView, holder.binding.connect)
    }

    fun put(element: ScanResult) {
        val index = items.deviceIndexOf(element)
        if (index >= 0) items[index] = element else items.add(element)
        if (keyword?.isNotEmpty() == true) {
            keyword?.let { key ->
                val name = element.deviceName
                if (name?.isNotEmpty() == true && name.contains(key, true)) {
                    val position = list.deviceIndexOf(element)
                    if (position >= 0) {
                        list[position] = element
                        notifyItemChanged(position)
                    } else {
                        list.add(element)
                        notifyItemInserted(itemCount - 1)
                    }
                }
            }
        } else {
            if (index >= 0) notifyItemChanged(index) else notifyItemInserted(list.size - 1)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun List<ScanResult>.deviceIndexOf(element: ScanResult): Int {
        for (i in indices) if (element.device == get(i).device) return i
        return -1
    }

    override fun clear() {
        val itemCount = list.size
        list.clear()
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
    }

    val ScanResult.deviceName: String?
        get() {
            var name: String? = scanRecord?.deviceName
            if (name.isNullOrBlank()) name = device.name
            if (name.isNullOrBlank() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                name = device.alias
            }
            return name
        }
}