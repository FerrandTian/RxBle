package tk.limt.rxble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log.d
import tk.limt.rxble.model.RxGatt
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import tk.limt.rxble.model.Phy
import tk.limt.utils.hex

@SuppressLint("MissingPermission")
class RxBleOnSubscribe(
    private val context: Context,
    private val device: BluetoothDevice,
    var autoConnect: Boolean = false,
) : ObservableOnSubscribe<RxGatt> {
    private val TAG = this.javaClass.simpleName
    var connectionState: Int = BluetoothProfile.STATE_DISCONNECTED
    var realGatt: BluetoothGatt? = null
    val gatt: BluetoothGatt
        get() {
            requireNotNull(realGatt) { "GATT client has not been established or has been closed" }
            return realGatt!!
        }

    override fun subscribe(emitter: ObservableEmitter<RxGatt>) {
        connectionState = BluetoothProfile.STATE_CONNECTING
        realGatt = device.connectGatt(context, autoConnect, object : BluetoothGattCallback() {
            override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
                d(TAG, "onPhyUpdate: $status, txPhy: $txPhy, rxPhy: $rxPhy")
                emitter.onNext(RxGatt.PhyUpdate(gatt, Phy(txPhy, rxPhy), status))
            }

            override fun onPhyRead(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
                d(TAG, "onPhyRead: $status, txPhy: $txPhy, rxPhy: $rxPhy")
                emitter.onNext(RxGatt.PhyRead(gatt, Phy(txPhy, rxPhy), status))
            }

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                d(TAG, "onConnectionStateChange: $status, newState: $newState")
                connectionState = newState
                emitter.onNext(RxGatt.ConnectionStateChange(gatt, status, newState))
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                d(TAG, "onServicesDiscovered: $status")
                emitter.onNext(RxGatt.ServicesDiscovered(gatt, status))
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int,
            ) {
                d(TAG, "onCharacteristicRead: $status, value: ${characteristic.value.hex()}")
                emitter.onNext(RxGatt.CharacteristicRead(gatt, characteristic, status))
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int,
            ) {
                d(TAG, "onCharacteristicWrite: $status, value: ${characteristic.value.hex()}")
                emitter.onNext(RxGatt.CharacteristicWrite(gatt, characteristic, status))
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
            ) {
                d(TAG, "onCharacteristicChanged: ${characteristic.value.hex()}")
                emitter.onNext(RxGatt.CharacteristicChanged(gatt, characteristic))
            }

            override fun onDescriptorRead(
                gatt: BluetoothGatt,
                descriptor: BluetoothGattDescriptor,
                status: Int,
            ) {
                d(TAG, "onDescriptorRead: $status, value: ${descriptor.value.hex()}")
                emitter.onNext(RxGatt.DescriptorRead(gatt, descriptor, status))
            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt,
                descriptor: BluetoothGattDescriptor,
                status: Int,
            ) {
                d(TAG, "onDescriptorWrite: $status, value: ${descriptor.value.hex()}")
                emitter.onNext(RxGatt.DescriptorWrite(gatt, descriptor, status))
            }

            override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
                d(TAG, "onReliableWriteCompleted: $status")
                emitter.onNext(RxGatt.ReliableWriteCompleted(gatt, status))
            }

            override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
                d(TAG, "onReadRemoteRssi: $status, rssi: $rssi")
                emitter.onNext(RxGatt.ReadRemoteRssi(gatt, rssi, status))
            }

            override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                d(TAG, "onMtuChanged: $status, mtu: $mtu")
                emitter.onNext(RxGatt.MtuChanged(gatt, mtu, status))
            }

            override fun onServiceChanged(gatt: BluetoothGatt) {
                d(TAG, "onServiceChanged: ")
                emitter.onNext(RxGatt.ServiceChanged(gatt))
            }
        }, BluetoothDevice.TRANSPORT_LE)
//        emitter.onNext(
//            RxGatt.ConnectionStateChange(
//                gatt,
//                BluetoothGatt.GATT_SUCCESS,
//                BluetoothProfile.STATE_CONNECTING
//            )
//        )
        emitter.setCancellable {
//            try {
            connectionState = BluetoothProfile.STATE_DISCONNECTED
            realGatt?.close()
            realGatt = null
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
        }
    }
}