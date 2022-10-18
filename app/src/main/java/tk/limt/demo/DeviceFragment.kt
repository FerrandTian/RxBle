package tk.limt.demo

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import tk.limt.demo.databinding.ItemServiceBinding
import tk.limt.demo.databinding.RecyclerBinding
import tk.limt.rxble.RxBle
import tk.limt.rxble.RxBleManager
import java.util.concurrent.TimeUnit

class DeviceFragment : Fragment(), OnItemClickListener<ItemServiceBinding, BluetoothGattService> {

    private var _binding: RecyclerBinding? = null
    private val binding get() = _binding!!
    private val aty get() = requireActivity() as AppCompatActivity
    private val adapter: ServiceAdapter = ServiceAdapter(this)
    private var mnProgress: MenuItem? = null
    private var mnConnect: MenuItem? = null
    private val bleManager = RxBleManager.instance
    private lateinit var device: BluetoothDevice
    private lateinit var ble: RxBle
    private lateinit var disState: Disposable
    private var disDiscover: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = RecyclerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
//        (binding.recycler.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        binding.recycler.addItemDecoration(
            DividerItemDecoration(aty, DividerItemDecoration.HORIZONTAL)
        )
        binding.recycler.adapter = adapter
        device = requireArguments().getParcelable("ARG_DATA")!!
        ble = bleManager.obtain(device.address)
        disState = ble.connectionState().observeOn(AndroidSchedulers.mainThread()).subscribe {
            updateUiWithData(it)
        }
        connectAndDiscoverServices()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_device, menu)
        mnProgress = menu.findItem(R.id.progress)
        mnConnect = menu.findItem(R.id.connect)
        updateUiWithData(ble.connectionState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.connect -> {
                if (ble.isConnected) {
                    ble.disconnect()
                } else {
                    connectAndDiscoverServices()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun connectAndDiscoverServices() {
        if (disDiscover == null || disDiscover?.isDisposed == true) {
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
                    mnConnect?.isVisible = false
                    mnProgress?.isVisible = true
                }

                override fun onSuccess(t: List<BluetoothGattService>) {
                    adapter.clear()
                    adapter.addAll(t)
                    adapter.notifyDataSetChanged()
                    disDiscover?.dispose()
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    disDiscover?.dispose()
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
                mnConnect?.isVisible = true
                mnProgress?.isVisible = false
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                mnConnect?.title = aty.getString(R.string.connect)
                mnConnect?.isVisible = true
                mnProgress?.isVisible = false
            }
            else -> {
                mnConnect?.isVisible = false
                mnProgress?.isVisible = true
            }
        }
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