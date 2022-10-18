package tk.limt.demo

import android.Manifest
import android.bluetooth.BluetoothAdapter
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
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import tk.limt.demo.databinding.ItemScanBinding
import tk.limt.demo.databinding.AppbarRefreshBinding
import tk.limt.rxble.RxBleManager
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    OnItemClickListener<ItemScanBinding, ScanResult>, MenuProvider,
    SearchView.OnQueryTextListener {

    private var _binding: AppbarRefreshBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AppbarRefreshBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        aty.setSupportActionBar(binding.toolbar)
        aty.addMenuProvider(this, viewLifecycleOwner)
        (binding.recycler.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        binding.recycler.addItemDecoration(
            SpacingItemDecoration(
                requireContext().dp2px(8f).toInt()
            ).includeEdge(true)
        )
        binding.recycler.adapter = adapter
        binding.refresh.isRefreshing = true
        onRefresh()
        binding.refresh.setOnRefreshListener(this)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.search, menu)
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return true
    }

    override fun onRefresh() {
        if (checkPermissions() && checkBluetooth() && checkLocation()) {
            adapter.clear()
            disposableScan?.dispose()
            disposableScan = bleManager.scan(
                null,
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
            ).observeOn(AndroidSchedulers.mainThread()).doOnDispose {
                binding.refresh.isRefreshing = false
            }.subscribe {
                adapter.put(it.result)
            }
            disposableTimer?.dispose()
            disposableTimer = Completable.complete().delay(5, TimeUnit.SECONDS).subscribe {
                disposableScan?.dispose()
                disposableTimer?.dispose()
            }
        } else {
            binding.refresh.isRefreshing = false
        }
    }

    override fun onItemClick(
        view: View,
        holder: Holder<ItemScanBinding>,
        item: ScanResult
    ) {
        disposableScan?.dispose()
        disposableTimer?.dispose()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        println("onQueryTextChange: $newText")
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
        if (!requireContext().isLocationEnabled()) {
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
}