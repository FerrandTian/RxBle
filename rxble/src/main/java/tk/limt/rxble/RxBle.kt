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
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Notification
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.toObservable
import io.reactivex.rxjava3.schedulers.Schedulers
import tk.limt.rxble.model.RxGatt.*
import tk.limt.utils.split
import java.util.*
import java.util.concurrent.TimeUnit

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
    private var writeEmitter: ObservableEmitter<Pair<ByteArray, BluetoothGattCharacteristic>>? =
        null
    private val writeObservable = Observable.create {
        writeEmitter = it
    }.concatMapSingle<Triple<
            ByteArray,
            BluetoothGattCharacteristic,
            Notification<Pair<ByteArray, BluetoothGattCharacteristic>>
            >> { pair ->
        (if (pair.first.size > mtu) pair.first.split(mtu).toObservable(
        ) else Observable.just(pair.first)).concatMapCompletable { value ->
            bleObservable.ofType(CharacteristicWrite::class.java).takeWhile {
                it.characteristic.uuid != pair.second.uuid
            }.ignoreElements().mergeWith(Completable.fromAction {
                pair.second.value = value
                check(source.gatt.writeCharacteristic(pair.second)) { "writeCharacteristic failed" }
            }).timeout(200, TimeUnit.MILLISECONDS)
        }.materialize<Pair<ByteArray, BluetoothGattCharacteristic>>().map {
            Triple(pair.first, pair.second, it)
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
     * Returns a {@link android.bluetooth.BluetoothGattService}, if the requested UUID is
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
     * Dispose all resources and close this Bluetooth GATT client. Application will not
     * receive any emits from this remote device after call this method.
     * Application should call this method as early as possible after it is done with
     * this GATT client.
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
     * The connection state.
     *
     * @return Can be one of {@link BluetoothProfile#STATE_DISCONNECTED}
     * or {@link BluetoothProfile#STATE_CONNECTED}
     */
    val connectionState: Int
        get() = source.connectionState

    val isDisconnected: Boolean
        get() = connectionState == BluetoothProfile.STATE_DISCONNECTED

    val isConnected: Boolean
        get() = connectionState == BluetoothProfile.STATE_CONNECTED

    val isWriteEnabled: Boolean
        get() = writeEmitter?.isDisposed == false
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
     * @return The new {@code Observable} that emits the new connection state.
     */
    fun connectionState() = bleObservable.ofType(ConnectionStateChange::class.java).map {
        if (it.state != BluetoothProfile.STATE_CONNECTING
            && it.state != BluetoothProfile.STATE_CONNECTED
        ) disableWrite()
        it.state
    }

    /**
     * Disconnects an established connection, or cancels a connection attempt
     * currently in progress.
     *
     * @return The new {@code Observable} that emits the new connection state.
     */
    fun disconnectWithState() = connectionState().mergeWith(Completable.fromAction {
        disconnect()
    })

    /**
     * Connect to GATT Server hosted by this device. Caller acts as GATT client.
     * Or re-connect after the connection has been dropped. If the device is not
     * in range, the re-connection will be triggered once the device is back in range.
     *
     * @return true, if the connection attempt was initiated successfully
     */
    fun connect() = if (bleDisposable == null || bleDisposable?.isDisposed == true) {
        bleDisposable = bleObservable.connect()
        true
    } else source.realGatt?.connect() == true

    /**
     * Connect to remote device.
     *
     * @return The new {@code Observable} that emits the new connection state.
     */
    fun connectWithState() = connectionState().mergeWith(Completable.fromAction {
        check(connect()) { "connect failed" }
    })

    /**
     * Connect and discover services to remote device.
     *
     * @return The new {@code Single} that emits the discovered services.
     */
    fun connectWithServices() = connectWithState().takeWhile {
        it != BluetoothProfile.STATE_CONNECTED
    }.ignoreElements().andThen(discoverServices())

    /**
     * Discovers services offered by a remote device as well as their
     * characteristics and descriptors.
     *
     * @return The new {@code Single} that emits the discovered services.
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
     * Indicates that the given characteristic has changed.
     *
     * @param uuid UUID of which Characteristic to subscribe
     * @return The new {@code Observable} that emits the Characteristic.
     */
    fun characteristic(uuid: UUID) = bleObservable.ofType(
        CharacteristicChanged::class.java
    ).mapOptional {
        if (it.characteristic.uuid == uuid) Optional.of(
            it.characteristic
        ) else Optional.empty()
    }

    /**
     * Reads the requested characteristic from the associated remote device.
     *
     * @param characteristic Characteristic to read from the remote device
     * @return The new {@code Single} emits the characteristic that was read successfully.
     */
    fun read(characteristic: BluetoothGattCharacteristic) = bleObservable.ofType(
        CharacteristicRead::class.java
    ).filter {
        it.characteristic.uuid == characteristic.uuid
    }.mergeWith(Completable.fromAction {
        check(source.gatt.readCharacteristic(characteristic)) { "readCharacteristic failed" }
    }).firstOrError().map { it.characteristic }

    /**
     * Writes a given characteristic and its values to the associated remote device.
     *
     * @param characteristic Characteristic to write on the remote device
     * @return The new {@code Single} emits the characteristic that was successfully written.
     */
    fun write(characteristic: BluetoothGattCharacteristic) = bleObservable.ofType(
        CharacteristicWrite::class.java
    ).filter {
        it.characteristic.uuid == characteristic.uuid
    }.mergeWith(Completable.fromAction {
        check(source.gatt.writeCharacteristic(characteristic)) { "writeCharacteristic failed" }
    }).firstOrError().map { it.characteristic }

    /**
     * Writes a bunch of values into the given characteristic to the associated remote device.
     *
     * <p>This function will commit all queued up characteristic write
     * operations for a given remote device.
     *
     * @param values A bunch of values that will be written
     * @param characteristic Characteristic to write on the remote device
     * @return A {@code Completable} completes the write operation.
     */
    fun write(
        values: Iterable<ByteArray>,
        characteristic: BluetoothGattCharacteristic
    ) = values.toObservable().concatMapCompletable { value ->
        characteristic.value = value
        write(characteristic).ignoreElement()
    }

    /**
     * Executes a reliable write transaction for a given remote device.
     *
     * <p>This function will commit all queued up characteristic write
     * operations for a given remote device.
     *
     * @param values A bunch of values that will be written
     * @param characteristic Characteristic to write on the remote device
     * @return A {@code Completable} completes the reliable write transaction.
     */
    fun reliableWrite(
        values: Iterable<ByteArray>,
        characteristic: BluetoothGattCharacteristic
    ) = values.toObservable().startWith(Completable.fromAction {
        check(source.gatt.beginReliableWrite()) { "beginReliableWrite failed" }
    }).concatMapCompletable { value ->
        characteristic.value = value
        write(characteristic).ignoreElement()
    }.andThen(
        bleObservable.ofType(ReliableWriteCompleted::class.java).firstOrError().ignoreElement(
        ).mergeWith(Completable.fromAction {
            check(source.gatt.executeReliableWrite()) { "executeReliableWrite failed" }
        })
    ).doOnError { source.gatt.abortReliableWrite() }

    fun writeInQueue(value: ByteArray, characteristic: BluetoothGattCharacteristic) {
        requireNotNull(writeEmitter) { "writeCharacteristic failed" }
        writeEmitter!!.onNext(Pair(value, characteristic))
    }

    fun write(
        value: ByteArray,
        characteristic: BluetoothGattCharacteristic
    ) = writeObservable.filter {
        it.second.uuid == characteristic.uuid
    }.mergeWith(Completable.fromAction {
        writeInQueue(value, characteristic)
    }).firstOrError().map { it.third }.dematerialize { it }.ignoreElement()

    /**
     * Reads the value for a given descriptor from the associated remote device.
     *
     * @param descriptor Descriptor value to read from the remote device
     * @return The new {@code Single} emits the descriptor that was read successfully.
     */
    fun read(descriptor: BluetoothGattDescriptor) = bleObservable.ofType(
        DescriptorRead::class.java
    ).filter {
        it.descriptor.uuid == descriptor.uuid
    }.mergeWith(Completable.fromAction {
        check(source.gatt.readDescriptor(descriptor)) { "readDescriptor failed" }
    }).firstOrError().map { it.descriptor }

    /**
     * Write the value of a given descriptor to the associated remote device.
     *
     * @param descriptor Descriptor to write to the associated remote device
     * @return The new {@code Single} emits the descriptor that was successfully written.
     */
    fun write(descriptor: BluetoothGattDescriptor) = bleObservable.ofType(
        DescriptorWrite::class.java
    ).filter {
        it.descriptor.uuid == descriptor.uuid
    }.mergeWith(Completable.fromAction {
        check(source.gatt.writeDescriptor(descriptor)) { "writeDescriptor failed" }
    }).firstOrError().map { it.descriptor }

    /**
     * Enable or disable notifications/indications for a given characteristic.
     *
     * @param descriptor Descriptor to write to the associated remote device.
     * Descriptor value can be one of {@link BluetoothGattDescriptor#ENABLE_NOTIFICATION_VALUE},
     * {@link BluetoothGattDescriptor#ENABLE_INDICATION_VALUE} or
     * {@link BluetoothGattDescriptor#DISABLE_NOTIFICATION_VALUE}
     * @return The new {@code Single} emits the descriptor that was successfully written.
     */
    fun setNotification(descriptor: BluetoothGattDescriptor) = write(
        descriptor
    ).startWith(Completable.fromAction {
        check(
            source.gatt.setCharacteristicNotification(
                descriptor.characteristic,
                !BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE.contentEquals(descriptor.value)
            )
        ) { "setCharacteristicNotification failed" }
    }).firstOrError()

    /**
     * Read the RSSI for a connected remote device.
     *
     * @return The new {@code Single} that emits the RSSI value.
     */
    fun readRemoteRssi() = bleObservable.ofType(ReadRemoteRssi::class.java).mergeWith(
        Completable.fromAction {
            check(source.gatt.readRemoteRssi()) { "readRemoteRssi failed" }
        }
    ).firstOrError().map { it.rssi }

    /**
     * Indicates the MTU for a given device connection has changed.
     *
     * @return The new {@code Observable} that emits the new MTU size.
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
     * @return The new {@code Single} that emits the new MTU size whether
     * this operation was successful.
     */
    fun requestMtu(mtu: Int) = mtu().mergeWith(Completable.fromAction {
        check(source.gatt.requestMtu(mtu)) { "requestMtu failed" }
    }).firstOrError()

    /**
     * Indicates service changed event is received.
     *
     * <p>Receiving this event means that the GATT database is out of sync with
     * the remote device. {@link BluetoothGatt#discoverServices} should be
     * called to re-discover the services.
     *
     * @return The new {@code Observable} that emits the old service list.
     */
    fun service() = bleObservable.ofType(ServiceChanged::class.java).map {
        disableWrite()
        services
    }
}