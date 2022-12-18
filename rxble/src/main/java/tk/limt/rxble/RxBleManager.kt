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
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import io.reactivex.rxjava3.core.Observable
import tk.limt.rxble.model.RxScanResult

class RxBleManager(private val context: Context) {
    @JvmField
    var manager = context.getSystemService(BluetoothManager::class.java)

    @JvmField
    var adapter = manager.adapter

    /**
     * Get the current connection state of the GATT device.
     *
     * <p>This is not specific to any application configuration but represents
     * the connection state of the local Bluetooth adapter for certain profile.
     * This can be used by applications like status bar which would just like
     * to know the state of Bluetooth.
     *
     * @param address Remote bluetooth device address.
     * @return State of the device connection. One of {@link BluetoothProfile#STATE_CONNECTED},
     * {@link BluetoothProfile#STATE_CONNECTING}, {@link BluetoothProfile#STATE_DISCONNECTED},
     * {@link BluetoothProfile#STATE_DISCONNECTING}
     * @throws IllegalArgumentException if address is invalid
     */
    fun getConnectionState(address: String) = getConnectionState(getRemoteDevice(address))

    /**
     * Get the current connection state of the GATT device.
     *
     * <p>This is not specific to any application configuration but represents
     * the connection state of the local Bluetooth adapter for certain profile.
     * This can be used by applications like status bar which would just like
     * to know the state of Bluetooth.
     *
     * @param device Remote bluetooth device.
     * @return State of the device connection. One of {@link BluetoothProfile#STATE_CONNECTED},
     * {@link BluetoothProfile#STATE_CONNECTING}, {@link BluetoothProfile#STATE_DISCONNECTED},
     * {@link BluetoothProfile#STATE_DISCONNECTING}
     */
    fun getConnectionState(device: BluetoothDevice) = manager.getConnectionState(
        device, BluetoothGatt.GATT
    )

    /**
     * Get connected GATT devices.
     *
     * <p> Return the set of devices which are in state {@link BluetoothProfile#STATE_CONNECTED}
     *
     * <p>This is not specific to any application configuration but represents
     * the connection state of Bluetooth for this profile.
     * This can be used by applications like status bar which would just like
     * to know the state of Bluetooth.
     *
     * @return List of devices. The list will be empty on error.
     */
    fun getConnectedDevices() = manager.getConnectedDevices(BluetoothGatt.GATT)

    /**
     * Return true if the remote device is connected.
     * <p>Equivalent to:
     * <code>getConnectionState(address) == BluetoothProfile.STATE_CONNECTED</code>
     *
     * @param address Remote bluetooth device address.
     * @return true if the remote device is connected.
     * @throws IllegalArgumentException if address is invalid
     */
    fun isConnected(address: String) = isConnected(getRemoteDevice(address))

    /**
     * Return true if the remote device is connected.
     * <p>Equivalent to:
     * <code>getConnectionState(device) == BluetoothProfile.STATE_CONNECTED</code>
     *
     * @param device Remote bluetooth device.
     * @return true if the remote device is connected.
     */
    fun isConnected(device: BluetoothDevice) =
        getConnectionState(device) == BluetoothProfile.STATE_CONNECTED

    /**
     * Get a {@link BluetoothDevice} object for the given Bluetooth hardware
     * address.
     * <p>Valid Bluetooth hardware addresses must be upper case, in a format
     * such as "00:11:22:33:AA:BB". The helper {@link #checkBluetoothAddress} is
     * available to validate a Bluetooth address.
     * <p>A {@link BluetoothDevice} will always be returned for a valid
     * hardware address, even if this adapter has never seen that device.
     *
     * @param address valid Bluetooth MAC address
     * @throws IllegalArgumentException if address is invalid
     */
    fun getRemoteDevice(address: String) = adapter.getRemoteDevice(address)

    /**
     * Return true if Bluetooth is currently enabled and ready for use.
     * <p>Equivalent to:
     * <code>getBluetoothState() == STATE_ON</code>
     *
     * @return true if the local adapter is turned on
     */
    fun isBluetoothEnabled() = adapter.isEnabled

    /**
     * Turn on the local Bluetooth adapter&mdash;do not use without explicit
     * user action to turn on Bluetooth.
     * <p>This powers on the underlying Bluetooth hardware, and starts all
     * Bluetooth system services.
     * <p class="caution"><strong>Bluetooth should never be enabled without
     * direct user consent</strong>. If you want to turn on Bluetooth in order
     * to create a wireless connection, you should use the {@link
     * #ACTION_REQUEST_ENABLE} Intent, which will raise a dialog that requests
     * user permission to turn on Bluetooth. The {@link #enable()} method is
     * provided only for applications that include a user interface for changing
     * system settings, such as a "power manager" app.</p>
     * <p>This is an asynchronous call: it will return immediately, and
     * clients should listen for {@link #ACTION_STATE_CHANGED}
     * to be notified of subsequent adapter state changes. If this call returns
     * true, then the adapter state will immediately transition from {@link
     * #STATE_OFF} to {@link #STATE_TURNING_ON}, and some time
     * later transition to either {@link #STATE_OFF} or {@link
     * #STATE_ON}. If this call returns false then there was an
     * immediate problem that will prevent the adapter from being turned on -
     * such as Airplane mode, or the adapter is already turned on.
     *
     * @return true to indicate adapter startup has begun, or false on immediate error
     */
    fun enableBluetooth() = adapter.enable()

    /**
     * Turn off the local Bluetooth adapter&mdash;do not use without explicit
     * user action to turn off Bluetooth.
     * <p>This gracefully shuts down all Bluetooth connections, stops Bluetooth
     * system services, and powers down the underlying Bluetooth hardware.
     * <p class="caution"><strong>Bluetooth should never be disabled without
     * direct user consent</strong>. The {@link #disable()} method is
     * provided only for applications that include a user interface for changing
     * system settings, such as a "power manager" app.</p>
     * <p>This is an asynchronous call: it will return immediately, and
     * clients should listen for {@link #ACTION_STATE_CHANGED}
     * to be notified of subsequent adapter state changes. If this call returns
     * true, then the adapter state will immediately transition from {@link
     * #STATE_ON} to {@link #STATE_TURNING_OFF}, and some time
     * later transition to either {@link #STATE_OFF} or {@link
     * #STATE_ON}. If this call returns false then there was an
     * immediate problem that will prevent the adapter from being turned off -
     * such as the adapter already being turned off.
     *
     * @return true to indicate adapter shutdown has begun, or false on immediate error
     */
    fun disableBluetooth() = adapter.disable()

    /**
     * Start Bluetooth LE scan. For unfiltered scans, scanning is stopped on screen off
     * to save power. Scanning is resumed when screen is turned on again. To avoid this,
     * do filetered scanning by using proper {@link ScanFilter}.
     *
     * <p>
     * An app must have
     * {@link android.Manifest.permission#ACCESS_COARSE_LOCATION ACCESS_COARSE_LOCATION} permission
     * in order to get results. An App targeting Android Q or later must have
     * {@link android.Manifest.permission#ACCESS_FINE_LOCATION ACCESS_FINE_LOCATION} permission
     * in order to get results.
     *
     * @param filters {@link ScanFilter}s for finding exact BLE devices.
     * @param settings Settings for the scan.
     * @return The new {@code Observable} that emits the ScanResult.
     */
    fun scan(filters: List<ScanFilter>?, settings: ScanSettings?) = Observable.create(
        RxBleScanOnSubscribe(adapter.bluetoothLeScanner, filters, settings)
    ).ofType(RxScanResult.ScanResult::class.java).map { it.result }

    /**
     * Start Bluetooth LE scan. For unfiltered scans, scanning is stopped on screen off
     * to save power. Scanning is resumed when screen is turned on again. To avoid this,
     * do filetered scanning by using proper {@link ScanFilter}.
     *
     * <p>
     * An app must have
     * {@link android.Manifest.permission#ACCESS_COARSE_LOCATION ACCESS_COARSE_LOCATION} permission
     * in order to get results. An App targeting Android Q or later must have
     * {@link android.Manifest.permission#ACCESS_FINE_LOCATION ACCESS_FINE_LOCATION} permission
     * in order to get results.
     *
     * @param filters {@link ScanFilter}s for finding exact BLE devices.
     * @param settings Settings for the scan.
     * @return The new {@code Observable} that emits the list of ScanResult.
     */
    fun scanList(filters: List<ScanFilter>?, settings: ScanSettings?) = Observable.create(
        RxBleScanOnSubscribe(adapter.bluetoothLeScanner, filters, settings)
    ).ofType(RxScanResult.ScanResults::class.java).map { it.results }


    /**
     * Create o new instance of {@code RxBle}. You can use RxBle to conduct
     * GATT client operations.
     * <p>This class provides Bluetooth GATT functionality to enable communication
     * with Bluetooth Smart or Smart Ready devices.
     * @param address Remote bluetooth device address.
     * @param autoConnect Whether to directly connect to the remote device (false) or to
     * automatically connect as soon as the remote device becomes available (true).
     * @return The new {@code RxBle} instance.
     */
    fun create(address: String, autoConnect: Boolean = false) = create(
        getRemoteDevice(address), autoConnect
    )

    /**
     * Create o new instance of {@code RxBle}. You can use RxBle to conduct
     * GATT client operations.
     * <p>This class provides Bluetooth GATT functionality to enable communication
     * with Bluetooth Smart or Smart Ready devices.
     * @param device Remote bluetooth device.
     * @param autoConnect Whether to directly connect to the remote device (false) or to
     * automatically connect as soon as the remote device becomes available (true).
     * @return The new {@code RxBle} instance.
     */
    fun create(device: BluetoothDevice, autoConnect: Boolean = false) = RxBle(
        context, device, autoConnect
    )

    companion object {
        @JvmStatic
        lateinit var instance: RxBleManager

        @JvmStatic
        fun init(ctx: Context) {
            instance = RxBleManager(ctx)
        }
    }
}