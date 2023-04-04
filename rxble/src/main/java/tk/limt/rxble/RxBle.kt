/*
 * Copyright (C) 2022 TianFeng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tk.limt.rxble

import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.os.Parcelable
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.toObservable
import io.reactivex.rxjava3.schedulers.Schedulers
import tk.limt.rxble.model.RxGatt.*
import tk.limt.utils.split
import java.util.*

/**
 * Public API for the Bluetooth GATT Profile.
 *
 * <p>This class provides Bluetooth GATT functionality to enable communication
 * with Bluetooth Smart or Smart Ready devices.
 *
 * <p>To connect to a remote peripheral device, create a instance of this class
 * and call {@link RxBle#connect}.
 * GATT capable devices can be discovered using the Bluetooth device discovery or BLE
 * scan process.
 * @author tianfeng
 */
class RxBle(
    ctx: Context,
    val device: BluetoothDevice,
    autoConnect: Boolean = false,
) {
    private var bleDisposable: Disposable? = null
    private val source = RxBleOnSubscribe(ctx, device, autoConnect)
    private val bleObservable = Observable.create(source).subscribeOn(Schedulers.io()).publish()
    private var writeEmitter: ObservableEmitter<Triple<Parcelable, ByteArray, Int>>? = null
    private val writeObservable = Observable.create {
        writeEmitter = it
    }.concatMapSingle { triple ->
        (if (triple.second.size > mtu) triple.second.split(mtu).toObservable(
        ) else Observable.just(triple.second)).concatMapCompletable { value ->
            when (triple.first) {
                is BluetoothGattCharacteristic -> write(
                    triple.first as BluetoothGattCharacteristic, value, triple.third
                )
                is BluetoothGattDescriptor -> write(
                    triple.first as BluetoothGattDescriptor, value
                )
                else -> Completable.complete()
            }
        }.materialize<Pair<Parcelable, ByteArray>>().map {
            Triple(triple.first, triple.second, it)
        }
    }.subscribeOn(Schedulers.io()).publish()
    private var writeDisposable: Disposable? = null

    /**
     * Get MTU size of current connection. Default MTU size is 20.
     */
    val mtu: Int
        get() = source.mtu

    /**
     * Returns a list of GATT services offered by the remote device.
     *
     * <p>This function requires that service discovery has been completed
     * for the given device.
     *
     * @return List of services on the remote device. Returns an empty list if service discovery has
     * not yet been performed.
     */
    val services: List<BluetoothGattService>
        get() = source.gatt.services

    /**
     * Returns a {@link android.bluetooth#BluetoothGattService}, if the requested UUID is
     * supported by the remote device.
     *
     * <p>This function requires that service discovery has been completed
     * for the given device.
     *
     * <p>If multiple instances of the same service (as identified by UUID)
     * exist, the first instance of the service is returned.
     *
     * @param uuid UUID of the requested service
     * @return BluetoothGattService if supported, or null if the requested service is not offered by
     * the remote device.
     */
    fun getService(uuid: UUID) = source.gatt.getService(uuid)

    /**
     * Dispose all resources and close this Bluetooth GATT client. Application should
     * call this method as early as possible after it is done with this GATT client.
     */
    fun close() {
        disableWrite()
        bleDisposable?.dispose()
        bleDisposable = null
    }

    /**
     * Disconnects an established connection, or cancels a connection attempt
     * currently in progress.
     */
    fun disconnect() {
        disableWrite()
        source.realGatt?.disconnect()
    }

    /**
     * Get state of the profile connection.
     *
     * @return One of {@link BluetoothProfile#STATE_CONNECTED},
     * {@link BluetoothProfile#STATE_CONNECTING}, {@link BluetoothProfile#STATE_DISCONNECTED},
     * {@link BluetoothProfile#STATE_DISCONNECTING}
     */
    val connectionState: Int
        get() = source.connectionState

    val isDisconnected: Boolean
        get() = connectionState == BluetoothProfile.STATE_DISCONNECTED

    val isConnected: Boolean
        get() = connectionState == BluetoothProfile.STATE_CONNECTED

    val isWriteEnabled: Boolean
        get() = isConnected
                && writeEmitter?.isDisposed == false
                && writeDisposable?.isDisposed == false

    fun enableWrite() {
        if (writeDisposable == null
            || writeDisposable?.isDisposed == true
        ) writeDisposable = writeObservable.connect()
    }

    fun disableWrite() {
        writeDisposable?.dispose()
        writeDisposable = null
    }

    /**
     * Indicates when GATT client has connected/disconnected to/from a remote
     * GATT server.
     *
     * @return The new Observable that emits the new connection state.
     */
    fun connectionState() = bleObservable.ofType(ConnectionStateChange::class.java).map {
        if (it.state != BluetoothProfile.STATE_CONNECTED) disableWrite()
        it.state
    }

    /**
     * Disconnects an established connection, or cancels a connection attempt
     * currently in progress.
     *
     * @return The new Observable that emits the new connection state.
     */
    fun disconnectWithState() = connectionState().takeUntil {
        it == BluetoothProfile.STATE_DISCONNECTED
    }.mergeWith(Completable.fromAction {
        disconnect()
    })

    /**
     * Connect to GATT Server hosted by this device. Caller acts as GATT client.
     * Or re-connect after the connection has been dropped. If the device is not
     * in range, the re-connection will be triggered once the device is back in range.
     *
     * @return true, if the connection attempt was initiated successfully
     */
    fun connect() = if (bleDisposable == null
        || bleDisposable?.isDisposed == true
    ) {
        bleDisposable = bleObservable.connect()
        true
    } else source.realGatt?.connect() == true

    /**
     * Request a connection parameter update.
     *
     * <p>This function will send a connection parameter update request to the
     * remote device.
     *
     * @param connectionPriority Request a specific connection priority. Must be one of {@link
     * BluetoothGatt#CONNECTION_PRIORITY_BALANCED}, {@link BluetoothGatt#CONNECTION_PRIORITY_HIGH}
     * or {@link BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER}.
     * @throws IllegalArgumentException If the parameters are outside of their specified range.
     */
    fun requestConnectionPriority(
        connectionPriority: Int,
    ) = source.gatt.requestConnectionPriority(connectionPriority)

    /**
     * Connect to remote device.
     *
     * @return The new Observable that emits the new connection state.
     */
    fun connectWithState() = connectionState().mergeWith(Completable.fromAction {
        check(connect()) { "connect failed" }
    }).takeUntil {
        it == BluetoothProfile.STATE_CONNECTED
    }

    /**
     * Connect and discover services to remote device.
     *
     * @return The new Single that emits the discovered services.
     */
    fun connectWithServices() = connectWithState().ignoreElements().andThen(
        discoverServices()
    )

    /**
     * Discovers services offered by a remote device as well as their
     * characteristics and descriptors.
     *
     * @return The new Single that emits the discovered services.
     */
    fun discoverServices() = bleObservable.ofType(
        ServicesDiscovered::class.java
    ).mergeWith(Completable.fromAction {
        check(source.gatt.discoverServices()) { "discoverServices failed" }
    }).firstOrError().map {
        enableWrite()
        services
    }

    /**
     * Indicates service changed event is received.
     *
     * <p>Receiving this event means that the GATT database is out of sync with
     * the remote device. {@link BluetoothGatt#discoverServices} should be
     * called to re-discover the services.
     *
     * @return The new Observable that emits the old service list.
     */
    fun service() = bleObservable.ofType(ServiceChanged::class.java).map {
        disableWrite()
        services
    }

    /**
     * Indicates that the given characteristic has changed.
     *
     * @param uuid UUID of which Characteristic to subscribe
     * @return The new Observable that emits the Characteristic`s value.
     */
    fun characteristic(uuid: UUID) = bleObservable.ofType(
        CharacteristicChanged::class.java
    ).mapOptional {
        if (it.characteristic.uuid == uuid) Optional.of(
            it.value
        ) else Optional.empty()
    }

    /**
     * Reads the requested characteristic from the associated remote device.
     *
     * @param characteristic Characteristic to read from the remote device
     * @return The new Single emits the value that was read successfully.
     */
    fun read(characteristic: BluetoothGattCharacteristic) = bleObservable.ofType(
        CharacteristicRead::class.java
    ).filter {
        it.characteristic.uuid == characteristic.uuid
    }.mergeWith(Completable.fromAction {
        check(source.gatt.readCharacteristic(characteristic)) { "readCharacteristic failed" }
    }).firstOrError().map {
        check(it.isSuccess) { "readCharacteristic failed, gatt status: ${it.status}" }
        it.value
    }

    /**
     * Writes a given characteristic and its values to the associated remote device.
     * This method is non-thread-safe.
     *
     * @param characteristic Characteristic to write on the remote device
     * @param value New value for this characteristic
     * @param writeType The write type to for this characteristic. Can be one of: {@link
     * BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT}, {@link
     * BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE} or {@link
     * BluetoothGattCharacteristic#WRITE_TYPE_SIGNED}.
     * @return A Completable completes the write transaction.
     */
    fun write(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
    ) = bleObservable.ofType(CharacteristicWrite::class.java).filter {
        it.characteristic.uuid == characteristic.uuid
    }.firstOrError().flatMapCompletable {
        check(it.isSuccess) { "writeCharacteristic failed, gatt status: ${it.status}" }
        Completable.complete()
    }.mergeWith(Completable.fromAction {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            source.gatt.writeCharacteristic(characteristic, value, writeType).also {
                check(it == BluetoothStatusCodes.SUCCESS) {
                    "writeCharacteristic failed, bluetooth status: $it"
                }
            }
        } else {
            characteristic.value = value
            characteristic.writeType = writeType
            check(source.gatt.writeCharacteristic(characteristic)) {
                "writeCharacteristic failed"
            }
        }
    })

    /**
     * Executes a reliable write transaction for a given remote device.
     *
     * <p>This function will commit all queued up characteristic write
     * operations for a given remote device.
     *
     * @param iterator A bunch of values and Characteristics that will be written
     * @return A Completable completes the reliable write transaction.
     */
    fun reliableWrite(
        iterator: Iterable<Triple<BluetoothGattCharacteristic, ByteArray, Int>>,
    ) = iterator.toObservable().startWith(Completable.fromAction {
        check(source.gatt.beginReliableWrite()) { "beginReliableWrite failed" }
    }).concatMapCompletable { triple ->
        (if (triple.second.size > mtu) triple.second.split(mtu).toObservable(
        ) else Observable.just(triple.second)).concatMapCompletable { value ->
            write(triple.first, value, triple.third)
        }
    }.andThen(
        bleObservable.ofType(ReliableWriteCompleted::class.java).firstOrError().flatMapCompletable {
            check(it.isSuccess) { "executeReliableWrite failed, gatt status: ${it.status}" }
            Completable.complete()
        }.mergeWith(Completable.fromAction {
            check(source.gatt.executeReliableWrite()) { "executeReliableWrite failed" }
        })
    ).doOnError { source.gatt.abortReliableWrite() }

    /**
     * Writes a given characteristic and its values sequentially to the associated
     * remote device. This method is thread-safe.
     *
     * @param characteristic Characteristic to write on the remote device
     * @param value New value for this characteristic
     * @param writeType The write type to for this characteristic. Can be one of: {@link
     * BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT}, {@link
     * BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE} or {@link
     * BluetoothGattCharacteristic#WRITE_TYPE_SIGNED}.
     */
    fun writeInQueue(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
    ) {
        check(isWriteEnabled) { "writeCharacteristic failed" }
        writeEmitter?.onNext(Triple(characteristic, value, writeType))
    }

    /**
     * Writes a given characteristic and its values sequentially to the associated
     * remote device. This method is thread-safe.
     *
     * @param characteristic Characteristic to write on the remote device
     * @param value New value for this characteristic
     * @param writeType The write type to for this characteristic. Can be one of: {@link
     * BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT}, {@link
     * BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE} or {@link
     * BluetoothGattCharacteristic#WRITE_TYPE_SIGNED}.
     * @return A Completable completes the write transaction.
     */
    fun writeWithQueue(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
    ) = writeObservable.filter {
        it.first is BluetoothGattCharacteristic
                && (it.first as BluetoothGattCharacteristic).uuid == characteristic.uuid
    }.mergeWith(Completable.fromAction {
        writeInQueue(characteristic, value, writeType)
    }).firstOrError().map { it.third }.dematerialize { it }.ignoreElement()

    /**
     * Reads the value for a given descriptor from the associated remote device.
     *
     * @param descriptor Descriptor value to read from the remote device
     * @return The new Single emits the descriptor's value that was read successfully.
     */
    fun read(descriptor: BluetoothGattDescriptor) = bleObservable.ofType(
        DescriptorRead::class.java
    ).filter {
        it.descriptor.uuid == descriptor.uuid
    }.mergeWith(Completable.fromAction {
        check(source.gatt.readDescriptor(descriptor)) { "readDescriptor failed" }
    }).firstOrError().map {
        check(it.isSuccess) { "readDescriptor failed, gatt status: ${it.status}" }
        it.value
    }

    /**
     * Write the value of a given descriptor to the associated remote device.
     * This method is non-thread-safe.
     *
     * @param descriptor Descriptor to write to the associated remote device
     * @param value New value for this descriptor
     * @return A Completable completes the write transaction.
     */
    fun write(
        descriptor: BluetoothGattDescriptor,
        value: ByteArray,
    ) = bleObservable.ofType(DescriptorWrite::class.java).filter {
        it.descriptor.uuid == descriptor.uuid
    }.firstOrError().flatMapCompletable {
        check(it.isSuccess) { "writeDescriptor failed, gatt status: ${it.status}" }
        Completable.complete()
    }.mergeWith(Completable.fromAction {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            source.gatt.writeDescriptor(descriptor, value).also {
                check(it == BluetoothStatusCodes.SUCCESS) {
                    "writeDescriptor failed, bluetooth status: $it"
                }
            }
        } else {
            descriptor.value = value
            check(source.gatt.writeDescriptor(descriptor)) {
                "writeDescriptor failed"
            }
        }
    })

    /**
     * Write the value of a given descriptor sequentially to the associated remote device.
     * This method is thread-safe.
     *
     * @param descriptor Descriptor to write to the associated remote device
     * @param value New value for this descriptor
     */
    fun writeInQueue(
        descriptor: BluetoothGattDescriptor,
        value: ByteArray,
    ) {
        check(isWriteEnabled) { "writeDescriptor failed" }
        writeEmitter?.onNext(
            Triple(
                descriptor, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        )
    }

    /**
     * Write the value of a given descriptor sequentially to the associated remote device.
     * This method is thread-safe.
     *
     * @param descriptor Descriptor to write to the associated remote device
     * @param value New value for this descriptor
     * @return A Completable completes the write transaction.
     */
    fun writeWithQueue(
        descriptor: BluetoothGattDescriptor,
        value: ByteArray,
    ) = writeObservable.filter {
        it.first is BluetoothGattDescriptor
                && (it.first as BluetoothGattDescriptor).uuid == descriptor.uuid
    }.mergeWith(Completable.fromAction {
        writeInQueue(descriptor, value)
    }).firstOrError().map { it.third }.dematerialize { it }.ignoreElement()

    /**
     * Enable or disable notifications/indications for a given characteristic.
     *
     * <p>Once notifications are enabled for a characteristic, a
     * {@link BluetoothGattCallback#onCharacteristicChanged} callback will be
     * triggered if the remote device indicates that the given characteristic
     * has changed.
     *
     * @param characteristic The characteristic for which to enable notifications
     * @param enable Set to true to enable notifications/indications
     * @return true, if the requested notification status was set successfully
     */
    fun setNotification(
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean,
    ) = source.gatt.setCharacteristicNotification(characteristic, enable)

    /**
     * Enable or disable notifications/indications for a given descriptor.
     *
     * @param descriptor Descriptor to write to the associated remote device.
     * @param value New value for this descriptor. Can be one of {@link
     * BluetoothGattDescriptor#ENABLE_NOTIFICATION_VALUE}, {@link
     * BluetoothGattDescriptor#ENABLE_INDICATION_VALUE} or {@link
     * BluetoothGattDescriptor#DISABLE_NOTIFICATION_VALUE}
     * @return A Completable completes the write transaction.
     */
    fun setNotification(
        descriptor: BluetoothGattDescriptor,
        value: ByteArray,
    ) = write(descriptor, value).startWith(Completable.fromAction {
        check(
            setNotification(
                descriptor.characteristic,
                !BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE.contentEquals(value)
            )
        ) { "setCharacteristicNotification failed" }
    })

    /**
     * Read the RSSI for a connected remote device.
     *
     * @return The new Single that emits the RSSI value.
     */
    fun readRemoteRssi() = bleObservable.ofType(ReadRemoteRssi::class.java).mergeWith(
        Completable.fromAction {
            check(source.gatt.readRemoteRssi()) { "readRemoteRssi failed" }
        }
    ).firstOrError().map { it.rssi }

    /**
     * Indicates the MTU for a given device connection has changed.
     *
     * @return The new Observable that emits the new MTU size.
     */
    fun mtu() = bleObservable.ofType(MtuChanged::class.java).map {
        it.mtu - 3
    }

    /**
     * Request an MTU size used for a given connection.
     *
     * <p>When performing a write request operation (write without response),
     * the data sent is truncated to the MTU size. This function may be used
     * to request a larger MTU size to be able to send more data at once.
     *
     * @param mtu The new MTU size
     * @return The new Single that emits the new MTU size.
     */
    fun requestMtu(mtu: Int) = mtu().mergeWith(Completable.fromAction {
        check(source.gatt.requestMtu(mtu)) { "requestMtu failed" }
    }).firstOrError()
}