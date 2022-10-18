package tk.limt.demo

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import tk.limt.demo.databinding.ActivityMainBinding
import tk.limt.demo.databinding.ItemScanBinding
import tk.limt.demo.ui.main.MainPagerAdapter

class MainActivity : AppCompatActivity(), OnTabChangeListener<BluetoothDevice> {

    private lateinit var binding: ActivityMainBinding
    private val adapter = MainPagerAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Scanner" else adapter[position - 1].name
                ?: adapter[position - 1].address
        }.attach()
    }

    override fun onTabChange(item: BluetoothDevice, open: Boolean) {
        val index = adapter.indexOf(item)
        if (open) {
            if (index >= 0) {
                binding.viewPager.currentItem = index + 1
            } else {
                adapter.add(item)
                adapter.notifyItemInserted(adapter.size)
                binding.viewPager.currentItem = adapter.size
            }
        } else {
            if (index >= 0) {
                adapter.remove(item)
                adapter.notifyItemRemoved(index + 1)
            }
        }
    }
}