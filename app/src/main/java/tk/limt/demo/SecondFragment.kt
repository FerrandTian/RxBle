package tk.limt.demo

import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import tk.limt.demo.databinding.FragmentSecondBinding
import tk.limt.rxble.RxBle
import tk.limt.rxble.RxBleManager
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment(), MenuProvider {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val aty get() = requireActivity() as AppCompatActivity
    private lateinit var menuItem: MenuItem
    private var loadingDialog: Dialog? = null
    private val bleManager = RxBleManager.instance
    private lateinit var device: BluetoothDevice
    private lateinit var ble: RxBle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        aty.setSupportActionBar(binding.toolbar)
        aty.addMenuProvider(this, viewLifecycleOwner)
        device = requireArguments().getParcelable("ARG_DATA")!!
        ble = bleManager.obtain(device.address)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_device, menu)
        menuItem = menu.findItem(R.id.connect)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.connect -> {
                if (ble.isConnected) {
                    ble.disconnect().apply {
                        menuItem.icon = aty.getDrawable(R.drawable.ic_baseline_bluetooth_24)
                    }
                } else {
                    ble.connectWithState().observeOn(
                        AndroidSchedulers.mainThread()
                    ).timeout(8, TimeUnit.SECONDS).retry(2).takeUntil {
                        it == BluetoothProfile.STATE_CONNECTED
                    }.doOnTerminate {
                        loadingDialog?.dismiss()
                    }.subscribe(object : Observer<Int> {
                        override fun onSubscribe(d: Disposable) {
                            showLoadingDialog("Connecting...")
                        }

                        override fun onNext(t: Int) {
                            if (t == BluetoothProfile.STATE_CONNECTED) {
                                menuItem.icon = aty.getDrawable(
                                    R.drawable.ic_baseline_bluetooth_disabled_24
                                )
                            }
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }

                        override fun onComplete() {
                        }
                    })
                }
                return true
            }
        }
        return false
    }

    fun showLoadingDialog(msg: String) {
        loadingDialog?.show() ?: AlertDialog.Builder(context)
            .setView(ProgressBar(context))
            .setMessage(msg)
            .show().apply {
                loadingDialog = this
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog?.cancel()
        _binding = null
    }
}