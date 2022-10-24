package tk.limt.rxble

import android.bluetooth.*
import android.content.Context
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

    fun connect() = if (bleDisposable == null || bleDisposable?.isDisposed == true) {
        bleDisposable = bleObservable.connect()
        true
    } else {
        source.realGatt?.connect() == true
    }

    fun connectWithState() = connectionState().doOnSubscribe {
        connect()
    }

    fun connectAndDiscoverServices() = connectWithState().filter {
        it == BluetoothProfile.STATE_CONNECTED
    }.firstOrError().flatMap { discoverServices() }

    fun discoverServices() =
        bleObservable.ofType(ServicesDiscovered::class.java).firstOrError().map {
            services
        }.doOnSubscribe {
            check(source.gatt.discoverServices()) { "discoverServices failed" }
        }

    fun value(characteristic: BluetoothGattCharacteristic) = bleObservable.ofType(
        CharacteristicChanged::class.java
    ).filter { it.characteristic.uuid == characteristic.uuid }.map {
        it.characteristic.value
    }

    fun read(characteristic: BluetoothGattCharacteristic) = bleObservable.ofType(
        CharacteristicRead::class.java
    ).filter { it.characteristic.uuid == characteristic.uuid }.firstOrError().map {
        it.characteristic
    }.doOnSubscribe {
        check(source.gatt.readCharacteristic(characteristic)) { "readCharacteristic failed" }
    }

    fun write(characteristic: BluetoothGattCharacteristic) = bleObservable.ofType(
        CharacteristicWrite::class.java
    ).filter { it.characteristic.uuid == characteristic.uuid }.firstOrError().map {
        it.characteristic
    }.doOnSubscribe {
        check(source.gatt.writeCharacteristic(characteristic)) { "writeCharacteristic failed" }
    }

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

    fun read(descriptor: BluetoothGattDescriptor) = bleObservable.ofType(
        DescriptorRead::class.java
    ).filter { it.descriptor.uuid == descriptor.uuid }.firstOrError().map {
        it.descriptor
    }.doOnSubscribe {
        check(source.gatt.readDescriptor(descriptor)) { "readDescriptor failed" }
    }

    fun write(descriptor: BluetoothGattDescriptor) = bleObservable.ofType(
        DescriptorWrite::class.java
    ).filter { it.descriptor.uuid == descriptor.uuid }.firstOrError().map {
        it.descriptor
    }.doOnSubscribe {
        check(source.gatt.writeDescriptor(descriptor)) { "writeDescriptor failed" }
    }

    fun setNotification(descriptor: BluetoothGattDescriptor) = write(
        descriptor
    ).doOnSubscribe {
        check(
            source.gatt.setCharacteristicNotification(
                descriptor.characteristic,
                !BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE.contentEquals(descriptor.value)
            )
        ) { "setCharacteristicNotification failed" }
    }

    fun readRemoteRssi() = bleObservable.ofType(ReadRemoteRssi::class.java).firstOrError().map {
        it.rssi
    }.doOnSubscribe {
        check(source.gatt.readRemoteRssi()) { "readRemoteRssi failed" }
    }

    fun mtu() = bleObservable.ofType(MtuChanged::class.java).map {
        mtu = it.mtu - 3
        it.mtu
    }

    fun requestMtu(mtu: Int) = mtu().firstOrError().doOnSubscribe {
        check(source.gatt.requestMtu(mtu)) { "requestMtu failed" }
    }

    fun services() = bleObservable.ofType(ServiceChanged::class.java).map { services }
}