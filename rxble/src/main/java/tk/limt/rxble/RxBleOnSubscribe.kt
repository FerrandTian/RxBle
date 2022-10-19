package tk.limt.rxble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import tk.limt.rxble.model.Phy
import tk.limt.rxble.model.RxGatt

@SuppressLint("MissingPermission")
internal class RxBleOnSubscribe(
    private val context: Context,
    private val device: BluetoothDevice,
    var autoConnect: Boolean = false,
) : ObservableOnSubscribe<RxGatt> {
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
                emitter.onNext(RxGatt.PhyUpdate(gatt, Phy(txPhy, rxPhy), status))
            }

            override fun onPhyRead(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
                emitter.onNext(RxGatt.PhyRead(gatt, Phy(txPhy, rxPhy), status))
            }

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                connectionState = newState
                emitter.onNext(RxGatt.ConnectionStateChange(gatt, status, newState))
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                emitter.onNext(RxGatt.ServicesDiscovered(gatt, status))
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int,
            ) {
                emitter.onNext(RxGatt.CharacteristicRead(gatt, characteristic, status))
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int,
            ) {
                emitter.onNext(RxGatt.CharacteristicWrite(gatt, characteristic, status))
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
            ) {
                emitter.onNext(RxGatt.CharacteristicChanged(gatt, characteristic))
            }

            override fun onDescriptorRead(
                gatt: BluetoothGatt,
                descriptor: BluetoothGattDescriptor,
                status: Int,
            ) {
                emitter.onNext(RxGatt.DescriptorRead(gatt, descriptor, status))
            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt,
                descriptor: BluetoothGattDescriptor,
                status: Int,
            ) {
                emitter.onNext(RxGatt.DescriptorWrite(gatt, descriptor, status))
            }

            override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
                emitter.onNext(RxGatt.ReliableWriteCompleted(gatt, status))
            }

            override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
                emitter.onNext(RxGatt.ReadRemoteRssi(gatt, rssi, status))
            }

            override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                emitter.onNext(RxGatt.MtuChanged(gatt, mtu, status))
            }

            override fun onServiceChanged(gatt: BluetoothGatt) {
                emitter.onNext(RxGatt.ServiceChanged(gatt))
            }
        }, BluetoothDevice.TRANSPORT_LE)
        emitter.setCancellable {
            connectionState = BluetoothProfile.STATE_DISCONNECTED
            realGatt?.close()
            realGatt = null
        }
    }
}