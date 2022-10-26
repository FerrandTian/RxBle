package tk.limt.demo.ui.main

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import tk.limt.demo.R
import tk.limt.demo.adapter.ScanAdapter
import tk.limt.demo.databinding.ItemScanBinding
import tk.limt.demo.databinding.RefreshBinding
import tk.limt.demo.impl.OnTabChangeListener
import tk.limt.rxble.RxBleManager
import tt.tt.component.TTFragment
import tt.tt.component.TTHolder
import tt.tt.component.TTItemClickListener
import tt.tt.rx.TTObserver
import tt.tt.utils.isBluetoothEnabled
import tt.tt.utils.isLocationEnabled
import tt.tt.utils.permissionGranted
import java.util.concurrent.TimeUnit

class ScanFragment : TTFragment(), SwipeRefreshLayout.OnRefreshListener,
    TTItemClickListener<ItemScanBinding, ScanResult>, SearchView.OnQueryTextListener {

    private var _binding: RefreshBinding? = null
    private val vb get() = _binding!!
    private val adapter: ScanAdapter = ScanAdapter(this)
    private val bleManager = RxBleManager.instance
    private val launcherPermissions = registerForActivityResult(RequestMultiplePermissions()) {}
    private val launcherBluetooth = registerForActivityResult(StartActivityForResult()) {}
    private val launcherLocation = registerForActivityResult(StartActivityForResult()) {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = RefreshBinding.inflate(inflater, container, false)
        return vb.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (vb.recycler.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        vb.recycler.addItemDecoration(
            DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL)
        )
        vb.recycler.adapter = adapter
        vb.refresh.isRefreshing = true
        onRefresh()
        vb.refresh.setOnRefreshListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search, menu)
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onRefresh() {
        if (checkPermissions() && checkBluetooth() && checkLocation()) {
            bleManager.scan(
                null,
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
            ).filter { it.device.name != null }.takeUntil(
                Observable.timer(10, TimeUnit.SECONDS)
            ).observeOn(AndroidSchedulers.mainThread()).doAfterTerminate {
                _binding?.refresh?.isRefreshing = false
            }.subscribe(object : TTObserver<ScanResult>(disposables) {
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    adapter.clear()
                }

                override fun onNext(t: ScanResult) {
                    adapter.put(t)
                }
            })
        } else _binding?.refresh?.isRefreshing = false
    }

    override fun onItemClick(
        view: View, holder: TTHolder<ItemScanBinding>, item: ScanResult
    ) {
        if (view == holder.vb.connect) {
            (ctx as OnTabChangeListener<BluetoothDevice>).onTabChange(item.device, true)
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        adapter.keyword = newText
        return true
    }

    private fun checkPermissions(): Boolean {
        val permissions: MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!ctx.permissionGranted(Manifest.permission.BLUETOOTH_SCAN)) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
        if (!ctx.permissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (permissions.isNotEmpty()) {
            launcherPermissions.launch(permissions.toTypedArray())
            return false
        }
        return true
    }

    private fun checkBluetooth(): Boolean {
        if (!ctx.isBluetoothEnabled) {
            launcherBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return false
        }
        return true
    }

    private fun checkLocation(): Boolean {
        if (!ctx.isLocationEnabled) {
            launcherLocation.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(): ScanFragment {
            return ScanFragment()
        }
    }
}