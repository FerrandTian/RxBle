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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.toObservable
import io.reactivex.rxjava3.schedulers.Schedulers
import tk.limt.rxble.model.RxGatt.*
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
     * Close this Bluetooth GATT client. Application will not receive any emits from
     * this remote device after call this method.
     *
     * Application should call this method as early as possible after it is done with
     * this GATT client.
     */
    fun close() {
        bleDisposable?.dispose()
        bleDisposable = null
    }

    /**
     * Disconnects an established connection, or cancels a connection attempt
     * currently in progress.
     */
    fun disconnect() {
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

    /**
     * Indicates when GATT client has connected/disconnected to/from a remote
     * GATT server.
     *
     * @return The new {@code Observable} that emits the new connection state.
     */
    fun connectionState() = bleObservable.ofType(ConnectionStateChange::class.java).map {
        it.state
    }

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
    } else {
        source.realGatt?.connect() == true
    }

    /**
     * Connect to remote device.
     *
     * @return The new {@code Observable} that emits the new connection state.
     */
    fun connectWithState() = Single.just(1).flatMapObservable {
        check(connect()) { "connect failed" }
        connectionState()
    }

    /**
     * Connect and discover services to remote device.
     *
     * @return The new {@code Single} that emits the discovered services.
     */
    fun connectWithServices() = connectWithState().filter {
        it == BluetoothProfile.STATE_CONNECTED
    }.firstOrError().flatMap { discoverServices() }

    /**
     * Discovers services offered by a remote device as well as their
     * characteristics and descriptors.
     *
     * @return The new {@code Single} that emits the discovered services.
     */
    fun discoverServices() = Single.just(1).flatMap {
        check(source.gatt.discoverServices()) { "discoverServices failed" }
        bleObservable.ofType(ServicesDiscovered::class.java).firstOrError().map { services }
    }

    /**
     * Indicates that the given characteristic has changed.
     *
     * @param uuid UUID of which Characteristic to subscribe
     * @return The new {@code Observable} that emits the Characteristic.
     */
    fun characteristic(uuid: UUID) = bleObservable.ofType(
        CharacteristicChanged::class.java
    ).filter { it.characteristic.uuid == uuid }.map { it.characteristic }

    /**
     * Reads the requested characteristic from the associated remote device.
     *
     * @param characteristic Characteristic to read from the remote device
     * @return The new {@code Single} emits the characteristic that was read successfully.
     */
    fun read(characteristic: BluetoothGattCharacteristic) = Single.just(1).flatMap {
        check(source.gatt.readCharacteristic(characteristic)) { "readCharacteristic failed" }
        bleObservable.ofType(CharacteristicRead::class.java).filter {
            it.characteristic.uuid == characteristic.uuid
        }.firstOrError().map { it.characteristic }
    }

    /**
     * Writes a given characteristic and its values to the associated remote device.
     *
     * @param characteristic Characteristic to write on the remote device
     * @return The new {@code Single} emits the characteristic that was successfully written.
     */
    fun write(characteristic: BluetoothGattCharacteristic) = Single.just(1).flatMap {
        check(source.gatt.writeCharacteristic(characteristic)) { "writeCharacteristic failed" }
        bleObservable.ofType(CharacteristicWrite::class.java).filter {
            it.characteristic.uuid == characteristic.uuid
        }.firstOrError().map { it.characteristic }
    }

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
        write(characteristic).flatMapCompletable {
            check(value.contentEquals(it.value)) { "writeCharacteristic failed" }
            Completable.complete()
        }
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
    ) = Single.just(1).flatMapCompletable {
        check(source.gatt.beginReliableWrite()) { "beginReliableWrite failed" }
        values.toObservable().concatMapCompletable { value ->
            characteristic.value = value
            write(characteristic).flatMapCompletable {
                check(value.contentEquals(it.value)) {
                    source.gatt.abortReliableWrite()
                    "writeCharacteristic failed"
                }
                Completable.complete()
            }
        }
    }.andThen(Single.just(1).flatMap {
        check(source.gatt.executeReliableWrite()) { "executeReliableWrite failed" }
        bleObservable.ofType(ReliableWriteCompleted::class.java).firstOrError()
    }.flatMapCompletable {
        Completable.complete()
    })

    /**
     * Reads the value for a given descriptor from the associated remote device.
     *
     * @param descriptor Descriptor value to read from the remote device
     * @return The new {@code Single} emits the descriptor that was read successfully.
     */
    fun read(descriptor: BluetoothGattDescriptor) = Single.just(1).flatMap {
        check(source.gatt.readDescriptor(descriptor)) { "readDescriptor failed" }
        bleObservable.ofType(DescriptorRead::class.java).filter {
            it.descriptor.uuid == descriptor.uuid
        }.firstOrError().map { it.descriptor }
    }

    /**
     * Write the value of a given descriptor to the associated remote device.
     *
     * @param descriptor Descriptor to write to the associated remote device
     * @return The new {@code Single} emits the descriptor that was successfully written.
     */
    fun write(descriptor: BluetoothGattDescriptor) = Single.just(1).flatMap {
        check(source.gatt.writeDescriptor(descriptor)) { "writeDescriptor failed" }
        bleObservable.ofType(DescriptorWrite::class.java).filter {
            it.descriptor.uuid == descriptor.uuid
        }.firstOrError().map { it.descriptor }
    }

    /**
     * Enable or disable notifications/indications for a given characteristic.
     *
     * @param descriptor Descriptor to write to the associated remote device.
     * Descriptor value can be one of {@link BluetoothGattDescriptor#ENABLE_NOTIFICATION_VALUE},
     * {@link BluetoothGattDescriptor#ENABLE_INDICATION_VALUE} or
     * {@link BluetoothGattDescriptor#DISABLE_NOTIFICATION_VALUE}
     * @return The new {@code Single} emits the descriptor that was successfully written.
     */
    fun setNotification(descriptor: BluetoothGattDescriptor) = Single.just(descriptor).flatMap {
        check(
            source.gatt.setCharacteristicNotification(
                descriptor.characteristic,
                !BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE.contentEquals(descriptor.value)
            )
        ) { "setCharacteristicNotification failed" }
        write(descriptor)
    }

    /**
     * Read the RSSI for a connected remote device.
     *
     * @return The new {@code Single} that emits the RSSI value.
     */
    fun readRemoteRssi() = Single.just(1).flatMap {
        check(source.gatt.readRemoteRssi()) { "readRemoteRssi failed" }
        bleObservable.ofType(ReadRemoteRssi::class.java).firstOrError().map { it.rssi }
    }

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
    fun requestMtu(mtu: Int) = Single.just(1).flatMap {
        check(source.gatt.requestMtu(mtu)) { "requestMtu failed" }
        mtu().firstOrError()
    }

    /**
     * Indicates service changed event is received.
     *
     * <p>Receiving this event means that the GATT database is out of sync with
     * the remote device. {@link BluetoothGatt#discoverServices} should be
     * called to re-discover the services.
     *
     * @return The new {@code Observable} that emits the old service list.
     */
    fun service() = bleObservable.ofType(ServiceChanged::class.java).map { services }
}