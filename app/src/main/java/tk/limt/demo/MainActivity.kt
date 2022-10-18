package tk.limt.demo

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import tk.limt.demo.ui.main.MainPagerAdapter
import tk.limt.demo.databinding.ActivityMainBinding
import tk.limt.demo.databinding.ItemScanBinding
import tk.limt.rxble.RxBleManager

class MainActivity : AppCompatActivity(), OnItemClickListener<ItemScanBinding, ScanResult> {

    private lateinit var binding: ActivityMainBinding
    private val adapter = MainPagerAdapter(this)
    private val bleManager = RxBleManager.instance
    private val launcherPermissions = registerForActivityResult(RequestMultiplePermissions()) {}
    private val launcherBluetooth = registerForActivityResult(StartActivityForResult()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Scan" else adapter[position - 1].name ?: adapter[position -1].address
        }.attach()

        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    override fun onItemClick(view: View, holder: Holder<ItemScanBinding>, item: ScanResult) {
        if (checkPermissions() && checkBluetooth()) {
            val index = adapter.indexOf(item.device)
            if (index >= 0) {
                binding.viewPager.currentItem = index + 1
            } else {
                adapter.add(item.device)
                adapter.notifyItemInserted(adapter.size)
                binding.viewPager.currentItem = adapter.size
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions: MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        if (permissions.isNotEmpty()) {
            launcherPermissions.launch(permissions.toTypedArray())
            return false
        }
        return true
    }

    private fun checkBluetooth(): Boolean {
        if (!bleManager.isBluetoothEnabled()) {
            launcherBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return false
        }
        return true
    }
}