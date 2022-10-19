package tk.limt.demo

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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import tk.limt.demo.databinding.ItemScanBinding
import tk.limt.demo.databinding.RefreshBinding
import tk.limt.rxble.RxBleManager
import java.util.concurrent.TimeUnit

class ScanFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    OnItemClickListener<ItemScanBinding, ScanResult>, SearchView.OnQueryTextListener {

    private var _binding: RefreshBinding? = null
    private val binding get() = _binding!!
    private val aty get() = requireActivity() as AppCompatActivity
    private val adapter: ScanAdapter = ScanAdapter(this)
    private val bleManager = RxBleManager.instance
    private var disposableScan: Disposable? = null
    private var disposableTimer: Disposable? = null
    private val launcherPermissions = registerForActivityResult(RequestMultiplePermissions()) {}
    private val launcherBluetooth = registerForActivityResult(StartActivityForResult()) {}
    private val launcherLocation = registerForActivityResult(StartActivityForResult()) {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = RefreshBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (binding.recycler.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        binding.recycler.addItemDecoration(
            DividerItemDecoration(aty, DividerItemDecoration.HORIZONTAL)
        )
        binding.recycler.adapter = adapter
        binding.refresh.isRefreshing = true
        onRefresh()
        binding.refresh.setOnRefreshListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search, menu)
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onRefresh() {
        if (checkPermissions() && checkBluetooth() && checkLocation()) {
            adapter.clear()
            disposableScan?.dispose()
            disposableScan = bleManager.scan(
                null,
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
            ).observeOn(AndroidSchedulers.mainThread()).doOnDispose {
                _binding?.refresh?.isRefreshing = false
            }.subscribe {
                adapter.put(it)
            }
            disposableTimer?.dispose()
            disposableTimer = Completable.complete().delay(5, TimeUnit.SECONDS).subscribe {
                disposableScan?.dispose()
                disposableTimer?.dispose()
            }
        } else {
            _binding?.refresh?.isRefreshing = false
        }
    }

    override fun onItemClick(
        view: View, holder: Holder<ItemScanBinding>, item: ScanResult
    ) {
        if (view == holder.binding.connect) {
            (activity as OnTabChangeListener<BluetoothDevice>).onTabChange(item.device, true)
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
            if (requireContext().checkPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
        if (requireContext().checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
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

    private fun checkLocation(): Boolean {
        if (!aty.isLocationEnabled()) {
            launcherLocation.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableScan?.dispose()
        disposableTimer?.dispose()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(): ScanFragment {
            return ScanFragment()
        }
    }
}