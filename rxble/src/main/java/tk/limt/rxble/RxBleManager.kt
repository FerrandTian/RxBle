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

class RxBleManager(var context: Context) {
    private var manager = context.getSystemService(BluetoothManager::class.java)
    private var adapter: BluetoothAdapter = manager.adapter
    private var bleMap: MutableMap<String, RxBle> = HashMap()

    fun getConnectionState(device: BluetoothDevice) = manager.getConnectionState(
        device, BluetoothGatt.GATT
    )

    fun isBluetoothEnabled() = adapter.isEnabled

    fun enableBluetooth() = adapter.enable()

    fun disableBluetooth() = adapter.disable()

    fun getRemoteDevice(address: String) = adapter.getRemoteDevice(address)

    fun scan(filters: List<ScanFilter>?, settings: ScanSettings?) = Observable.create(
        RxBleScanOnSubscribe(adapter.bluetoothLeScanner, filters, settings)
    ).ofType(RxScanResult.ScanResult::class.java).map { it.result }

    fun scanList(filters: List<ScanFilter>?, settings: ScanSettings?) = Observable.create(
        RxBleScanOnSubscribe(adapter.bluetoothLeScanner, filters, settings)
    ).ofType(RxScanResult.ScanResults::class.java).map { it.results }

    fun create(address: String, autoConnect: Boolean = false) = create(
        getRemoteDevice(address), autoConnect
    )

    fun create(device: BluetoothDevice, autoConnect: Boolean = false): RxBle {
//        check(bleMap.size <= 8) { "A maximum of 8 connections are supported" }
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
        lateinit var instance: RxBleManager

        @JvmStatic
        fun init(context: Context) {
            instance = RxBleManager(context)
        }
    }
}