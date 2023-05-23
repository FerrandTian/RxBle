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

package tk.limt.demo.data

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import tk.limt.rxble.RxBle
import tk.limt.rxble.RxBleManager

/**
 * Manage all connections to remote device.
 */
class DeviceManager(var ctx: Context) {
    private val bleManager = RxBleManager(ctx)
    private var bleMap: MutableMap<String, RxBle> = HashMap()

    fun scan(filters: List<ScanFilter>?, settings: ScanSettings?) =
        bleManager.scan(filters, settings)

    fun getConnectionState(device: BluetoothDevice) = bleManager.getConnectionState(device)

    fun isConnected(device: BluetoothDevice) = bleManager.isConnected(device)

    fun obtain(address: String) = bleMap[address] ?: bleManager.create(
        address, false
    ).also {
        bleMap[address] = it
    }

    fun close(address: String) = bleMap.remove(address)?.close()

    fun disconnectAll() = bleMap.forEach {
        it.value.disconnect()
    }

    fun closeAll() = bleMap.forEach {
        it.value.close()
    }.also {
        bleMap.clear()
    }

    companion object {
        @JvmStatic
        lateinit var instance: DeviceManager

        @JvmStatic
        fun init(ctx: Context) {
            instance = DeviceManager(ctx)
        }
    }
}