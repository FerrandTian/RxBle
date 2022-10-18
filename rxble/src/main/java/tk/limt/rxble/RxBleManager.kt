package tk.limt.rxble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import io.reactivex.rxjava3.core.Observable
import tk.limt.rxble.model.RxScanResult

class RxBleManager {
    private lateinit var context: Context
    private lateinit var manager: BluetoothManager
    private lateinit var adapter: BluetoothAdapter
    private var bleMap: MutableMap<String, RxBle> = HashMap()

    fun init(context: Context) {
        this.context = context
        this.manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        this.adapter = manager.adapter
    }

    @SuppressLint("MissingPermission")
    fun getConnectionState(device: BluetoothDevice) = manager.getConnectionState(
        device, BluetoothGatt.GATT
    )

    fun isBluetoothEnabled() = adapter.isEnabled

    @SuppressLint("MissingPermission")
    fun enableBluetooth() = adapter.enable()

    @SuppressLint("MissingPermission")
    fun disableBluetooth() = adapter.disable()

    fun getRemoteDevice(address: String) = adapter.getRemoteDevice(address)

    fun scan(filters: List<ScanFilter>?, settings: ScanSettings?) = Observable.create(
        RxBleScanOnSubscribe(adapter.bluetoothLeScanner, filters, settings)
    ).ofType(RxScanResult.ScanResult::class.java)

    fun create(address: String, autoConnect: Boolean = false) = create(
        getRemoteDevice(address), autoConnect
    )

    fun create(device: BluetoothDevice, autoConnect: Boolean = false): RxBle {
        val ble = RxBle(context, device, autoConnect)
        bleMap[device.address] = ble
        return ble
    }

    fun contains(address: String) = bleMap.containsKey(address)

    operator fun get(address: String) = bleMap[address]

    fun obtain(address: String, autoConnect: Boolean = false) = bleMap[address] ?: create(
        address, autoConnect
    )

    fun isConnected(address: String) = isConnected(getRemoteDevice(address))

    fun isConnected(device: BluetoothDevice) = bleMap[
            device.address
    ]?.isConnected == true && getConnectionState(device) == BluetoothProfile.STATE_CONNECTED

    fun connect(address: String, autoConnect: Boolean = false) = connect(
        getRemoteDevice(address), autoConnect
    )

    fun connect(device: BluetoothDevice, autoConnect: Boolean = false) = (bleMap[
            device.address
    ] ?: create(device, autoConnect)).connectWithState()

    fun disconnectAll() = bleMap.forEach {
        it.value.disconnect()
    }

    fun close(address: String) = close(getRemoteDevice(address))

    fun close(device: BluetoothDevice) = bleMap.remove(device.address)?.close()

    fun closeAll() = bleMap.forEach {
        it.value.close()
    }.also {
        bleMap.clear()
    }

    companion object {
        @JvmStatic
        val instance: RxBleManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RxBleManager()
        }
    }
}