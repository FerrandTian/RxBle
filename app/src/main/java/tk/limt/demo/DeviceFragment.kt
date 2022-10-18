package tk.limt.demo

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import tk.limt.demo.databinding.ItemServiceBinding
import tk.limt.demo.databinding.RefreshBinding
import tk.limt.rxble.RxBle
import tk.limt.rxble.RxBleManager
import java.util.concurrent.TimeUnit

class DeviceFragment : Fragment(), OnItemClickListener<ItemServiceBinding, BluetoothGattService>,
    SwipeRefreshLayout.OnRefreshListener {

    private var _binding: RefreshBinding? = null
    private val binding get() = _binding!!
    private val aty get() = requireActivity() as AppCompatActivity
    private val adapter: ServiceAdapter = ServiceAdapter(this)
    private var mnConnect: MenuItem? = null
    private val launcherPermissions = registerForActivityResult(RequestMultiplePermissions()) {}
    private val launcherBluetooth = registerForActivityResult(StartActivityForResult()) {}
    private val bleManager = RxBleManager.instance
    private lateinit var device: BluetoothDevice
    private lateinit var ble: RxBle
    private lateinit var disState: Disposable
    private var disDiscover: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = RefreshBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding.recycler.addItemDecoration(
            DividerItemDecoration(aty, DividerItemDecoration.HORIZONTAL)
        )
        binding.recycler.adapter = adapter
        device = requireArguments().getParcelable("ARG_DATA")!!
        ble = bleManager.obtain(device.address)
        disState = ble.connectionState().observeOn(AndroidSchedulers.mainThread()).subscribe {
            updateUiWithData(it)
        }
        binding.refresh.isRefreshing = true
        onRefresh()
        binding.refresh.setOnRefreshListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_device, menu)
        mnConnect = menu.findItem(R.id.connect)
        updateUiWithData(ble.connectionState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.connect -> {
                if (ble.isConnected) {
                    ble.disconnect()
                } else {
                    binding.refresh.isRefreshing = true
                    onRefresh()
                }
                return true
            }
            R.id.close -> {
                bleManager.close(device)
                (activity as OnTabChangeListener<BluetoothDevice>).onTabChange(device, false)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRefresh() {
        if (checkPermissions() && checkBluetooth() && disDiscover == null || disDiscover?.isDisposed == true) {
            (if (ble.isDisconnected) ble.connectWithState().timeout(8, TimeUnit.SECONDS).retry(
                2
            ).filter {
                it == BluetoothProfile.STATE_CONNECTED
            }.firstOrError() else Single.just(BluetoothProfile.STATE_CONNECTED)).flatMap {
                ble.discoverServices()
            }.observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe(object : SingleObserver<List<BluetoothGattService>> {
                override fun onSubscribe(d: Disposable) {
                    disDiscover = d
                }

                override fun onSuccess(t: List<BluetoothGattService>) {
                    adapter.clear()
                    adapter.addAll(t)
                    adapter.notifyDataSetChanged()
                    disDiscover?.dispose()
                    binding.refresh.isRefreshing = false
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    disDiscover?.dispose()
                    binding.refresh.isRefreshing = false
                    Toast.makeText(aty, e.message, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onItemClick(
        view: View,
        holder: Holder<ItemServiceBinding>,
        item: BluetoothGattService
    ) {
    }

    fun updateUiWithData(state: Int) {
        when (state) {
            BluetoothProfile.STATE_CONNECTED -> {
                mnConnect?.title = aty.getString(R.string.disconnect)
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                mnConnect?.title = aty.getString(R.string.connect)
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions: MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (requireContext().checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        disState.dispose()
        disDiscover?.dispose()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(device: BluetoothDevice): DeviceFragment {
            return DeviceFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("ARG_DATA", device)
                }
            }
        }
    }
}