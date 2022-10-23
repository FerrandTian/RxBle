package tk.limt.demo.adapter

import android.bluetooth.BluetoothDevice
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import tk.limt.demo.ui.main.DeviceFragment
import tk.limt.demo.ui.main.ScanFragment

class MainPagerAdapter(
    context: FragmentActivity,
    val items: MutableList<BluetoothDevice> = ArrayList()
) : FragmentStateAdapter(context), MutableList<BluetoothDevice> by items {

    override fun getItemCount(): Int {
        return items.size + 1
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) ScanFragment.newInstance() else DeviceFragment.newInstance(items[position - 1])
    }
}