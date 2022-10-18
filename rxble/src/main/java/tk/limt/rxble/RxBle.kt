package tk.limt.rxble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.toObservable
import io.reactivex.rxjava3.schedulers.Schedulers
import tk.limt.rxble.model.RxGatt.*
import java.util.*

class RxBle(
    context: Context,
    val device: BluetoothDevice,
    autoConnect: Boolean = false,
) {
    private var bleDisposable: Disposable? = null
    private val source = RxBleOnSubscribe(context, device, autoConnect)
    private val bleObservable = Observable.create(source).subscribeOn(Schedulers.io()).publish()
    private var mtu: Int = 20
    val services: List<BluetoothGattService>
        get() = source.gatt.services

    fun getService(uuid: UUID) = source.gatt.getService(uuid)

    fun close() {
        bleDisposable?.dispose()
        bleDisposable = null
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        source.realGatt?.disconnect()
    }

    val connectionState: Int
        get() = source.connectionState

    val isDisconnected: Boolean
        get() = connectionState == BluetoothProfile.STATE_DISCONNECTED

    val isConnected: Boolean
        get() = connectionState == BluetoothProfile.STATE_CONNECTED

    fun connectionState() = bleObservable.ofType(ConnectionStateChange::class.java).map {
        it.state
    }

    @SuppressLint("MissingPermission")
    fun connect() = if (bleDisposable == null || bleDisposable?.isDisposed == true) {
        bleDisposable = bleObservable.connect()
        true
    } else {
        source.realGatt?.connect() == true
    }

    @SuppressLint("MissingPermission")
    fun connectWithState() = connectionState().doOnSubscribe {
        connect()
    }

    fun connectAndDiscoverServices() = connectWithState().filter {
        it == BluetoothProfile.STATE_CONNECTED
    }.firstOrError().flatMap { discoverServices() }

    @RequiresApi(Build.VERSION_CODES.O)
    fun phy() = bleObservable.ofType(PhyUpdate::class.java).map { it.phy }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    fun setPreferredPhy(txPhy: Int, rxPhy: Int, phyOptions: Int) = phy().firstOrError(
    ).doOnSubscribe { source.gatt.setPreferredPhy(txPhy, rxPhy, phyOptions) }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    fun readPhy() = bleObservable.ofType(PhyRead::class.java).map {
        it.phy
    }.firstOrError().doOnSubscribe { source.gatt.readPhy() }

    @SuppressLint("MissingPermission")
    fun discoverServices() = bleObservable.ofType(ServicesDiscovered::class.java).map {
        services
    }.firstOrError().doOnSubscribe {
        check(source.gatt.discoverServices()) { "discoverServices failed" }
    }

    @SuppressLint("MissingPermission")
    fun read(characteristic: BluetoothGattCharacteristic) = bleObservable.ofType(
        CharacteristicRead::class.java
    ).filter { it.characteristic.uuid == characteristic.uuid }.map {
        it.characteristic
    }.firstOrError().doOnSubscribe {
        check(source.gatt.readCharacteristic(characteristic)) { "readCharacteristic failed" }
    }

    @SuppressLint("MissingPermission")
    fun write(characteristic: BluetoothGattCharacteristic) = bleObservable.ofType(
        CharacteristicWrite::class.java
    ).filter { it.characteristic.uuid == characteristic.uuid }.map {
        it.characteristic
    }.firstOrError().doOnSubscribe {
        check(source.gatt.writeCharacteristic(characteristic)) { "writeCharacteristic failed" }
    }

    @SuppressLint("MissingPermission")
    fun read(descriptor: BluetoothGattDescriptor) = bleObservable.ofType(
        DescriptorRead::class.java
    ).filter { it.descriptor.uuid == descriptor.uuid }.map {
        it.descriptor
    }.firstOrError().doOnSubscribe {
        check(source.gatt.readDescriptor(descriptor)) { "readDescriptor failed" }
    }

    @SuppressLint("MissingPermission")
    fun write(descriptor: BluetoothGattDescriptor) = bleObservable.ofType(
        DescriptorWrite::class.java
    ).filter { it.descriptor.uuid == descriptor.uuid }.firstOrError().flatMapCompletable {
        Completable.complete()
    }.doOnSubscribe {
        check(source.gatt.writeDescriptor(descriptor)) { "writeDescriptor failed" }
    }

    fun value(characteristic: BluetoothGattCharacteristic) = bleObservable.ofType(
        CharacteristicChanged::class.java
    ).filter { it.characteristic.uuid == characteristic.uuid }.map {
        it.characteristic.value
    }

    @SuppressLint("MissingPermission")
    fun reliableWrite(
        values: Iterable<ByteArray>,
        characteristic: BluetoothGattCharacteristic
    ) = values.toObservable().doOnSubscribe {
        check(source.gatt.beginReliableWrite()) { "beginReliableWrite failed" }
    }.concatMapCompletable { value ->
        characteristic.value = value
        write(characteristic).flatMapCompletable {
            check(!value.contentEquals(it.value)) {
                source.gatt.abortReliableWrite()
                "writeCharacteristic failed"
            }
            Completable.complete()
        }
    }.andThen(bleObservable.ofType(ReliableWriteCompleted::class.java).flatMapCompletable {
        Completable.complete()
    }.doOnSubscribe {
        check(source.gatt.executeReliableWrite()) { "executeReliableWrite failed" }
    })

    @SuppressLint("MissingPermission")
    fun readRemoteRssi() = bleObservable.ofType(ReadRemoteRssi::class.java).map {
        it.rssi
    }.firstOrError().doOnSubscribe {
        check(source.gatt.readRemoteRssi()) { "readRemoteRssi failed" }
    }

    fun mtu() = bleObservable.ofType(MtuChanged::class.java).map {
        mtu = it.mtu - 3
        it.mtu
    }

    @SuppressLint("MissingPermission")
    fun requestMtu(mtu: Int) = mtu().firstOrError().doOnSubscribe {
        check(source.gatt.requestMtu(mtu)) { "requestMtu failed" }
    }

    fun services() = bleObservable.ofType(ServiceChanged::class.java).map { services }
}