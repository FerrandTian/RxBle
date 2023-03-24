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

package tk.limt.rxble.model

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile

internal open class RxGatt(
    val gatt: BluetoothGatt,
    val status: Int = BluetoothGatt.GATT_SUCCESS,
    val state: Int = BluetoothProfile.STATE_CONNECTED,
) {

    val isSuccess: Boolean
        get() = status == BluetoothGatt.GATT_SUCCESS
    val isConnected: Boolean
        get() = state == BluetoothProfile.STATE_CONNECTED
    val isDisconnected: Boolean
        get() = state == BluetoothProfile.STATE_DISCONNECTED

    class PhyUpdate(gatt: BluetoothGatt, status: Int, val phy: Phy) : RxGatt(gatt, status)

    class PhyRead(gatt: BluetoothGatt, status: Int, val phy: Phy) : RxGatt(gatt, status)

    class ConnectionStateChange(
        gatt: BluetoothGatt,
        status: Int,
        newState: Int,
    ) : RxGatt(gatt, status, newState)

    class ServicesDiscovered(gatt: BluetoothGatt, status: Int) : RxGatt(gatt, status)

    class CharacteristicRead(
        gatt: BluetoothGatt,
        status: Int,
        val characteristic: BluetoothGattCharacteristic,
        val value: ByteArray = characteristic.value,
    ) : RxGatt(gatt, status)

    class CharacteristicWrite(
        gatt: BluetoothGatt,
        status: Int,
        val characteristic: BluetoothGattCharacteristic,
    ) : RxGatt(gatt, status)

    class CharacteristicChanged(
        gatt: BluetoothGatt,
        val characteristic: BluetoothGattCharacteristic,
        val value: ByteArray = characteristic.value,
    ) : RxGatt(gatt)

    class DescriptorRead(
        gatt: BluetoothGatt,
        status: Int,
        val descriptor: BluetoothGattDescriptor,
        val value: ByteArray = descriptor.value,
    ) : RxGatt(gatt, status)

    class DescriptorWrite(
        gatt: BluetoothGatt,
        status: Int,
        val descriptor: BluetoothGattDescriptor,
    ) : RxGatt(gatt, status)

    class ReliableWriteCompleted(gatt: BluetoothGatt, status: Int) : RxGatt(gatt, status)

    class ReadRemoteRssi(gatt: BluetoothGatt, status: Int, val rssi: Int) : RxGatt(gatt, status)

    class MtuChanged(gatt: BluetoothGatt, status: Int, val mtu: Int) : RxGatt(gatt, status)

    class ServiceChanged(gatt: BluetoothGatt) : RxGatt(gatt)
}