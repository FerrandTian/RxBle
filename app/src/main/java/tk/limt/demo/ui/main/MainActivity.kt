package tk.limt.demo.ui.main

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import tk.limt.demo.adapter.MainPagerAdapter
import tk.limt.demo.databinding.ActivityMainBinding
import tk.limt.demo.impl.OnTabChangeListener
import tt.tt.component.TTActivity

class MainActivity : TTActivity<ActivityMainBinding>(), OnTabChangeListener<BluetoothDevice> {

    private val adapter = MainPagerAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(vb.toolbar)
        vb.viewPager.adapter = adapter
        TabLayoutMediator(vb.tabs, vb.viewPager) { tab, position ->
            tab.text = if (position == 0) "Scanner" else adapter[position - 1].name
                ?: adapter[position - 1].address
        }.attach()
    }

    override fun onTabChange(item: BluetoothDevice, open: Boolean) {
        val index = adapter.indexOf(item)
        if (open) {
            if (index >= 0) {
                vb.viewPager.currentItem = index + 1
            } else {
                adapter.add(item)
                adapter.notifyItemInserted(adapter.size)
                vb.viewPager.currentItem = adapter.size
            }
        } else {
            if (index >= 0) {
                adapter.remove(item)
                adapter.notifyItemRemoved(index + 1)
            }
        }
    }
}