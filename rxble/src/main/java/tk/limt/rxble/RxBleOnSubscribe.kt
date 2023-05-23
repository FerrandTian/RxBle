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
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import tk.limt.rxble.model.Phy
import tk.limt.rxble.model.RxGatt
import java.lang.Exception

internal class RxBleOnSubscribe(
    private val ctx: Context,
    private val device: BluetoothDevice,
    var autoConnect: Boolean = false,
) : ObservableOnSubscribe<RxGatt>, BluetoothGattCallback() {
    private var emitter: ObservableEmitter<RxGatt>? = null
    private var realGatt: BluetoothGatt? = null
    val gatt: BluetoothGatt
        get() {
            requireNotNull(realGatt) { "GATT client has not been established or has been closed" }
            return realGatt!!
        }
    var mtu: Int = 20
        private set

    fun connect() {
        try {
            if (realGatt?.connect() != true) connectGatt()
        } catch (e: Exception) {
            e.printStackTrace()
            connectGatt()
        }
    }

    private fun connectGatt() {
        realGatt = device.connectGatt(ctx, autoConnect, this, BluetoothDevice.TRANSPORT_LE)
    }

    override fun subscribe(emitter: ObservableEmitter<RxGatt>) {
        emitter.setCancellable { close() }
        this.emitter = emitter
        connect()
    }

    override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
        emitter?.onNext(RxGatt.PhyUpdate(gatt, status, Phy(txPhy, rxPhy)))
    }

    override fun onPhyRead(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
        emitter?.onNext(RxGatt.PhyRead(gatt, status, Phy(txPhy, rxPhy)))
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        emitter?.onNext(RxGatt.ConnectionStateChange(gatt, status, newState))
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        emitter?.onNext(RxGatt.ServicesDiscovered(gatt, status))
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
    ) {
        emitter?.onNext(RxGatt.CharacteristicRead(gatt, status, characteristic))
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic,
        value: ByteArray, status: Int
    ) {
        emitter?.onNext(RxGatt.CharacteristicRead(gatt, status, characteristic, value))
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
    ) {
        emitter?.onNext(RxGatt.CharacteristicWrite(gatt, status, characteristic))
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic
    ) {
        emitter?.onNext(RxGatt.CharacteristicChanged(gatt, characteristic))
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray
    ) {
        emitter?.onNext(RxGatt.CharacteristicChanged(gatt, characteristic, value))
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
    ) {
        emitter?.onNext(RxGatt.DescriptorRead(gatt, status, descriptor))
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int, value: ByteArray
    ) {
        emitter?.onNext(RxGatt.DescriptorRead(gatt, status, descriptor, value))
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
    ) {
        emitter?.onNext(RxGatt.DescriptorWrite(gatt, status, descriptor))
    }

    override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
        emitter?.onNext(RxGatt.ReliableWriteCompleted(gatt, status))
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
        emitter?.onNext(RxGatt.ReadRemoteRssi(gatt, status, rssi))
    }

    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
        this.mtu = mtu - 3
        emitter?.onNext(RxGatt.MtuChanged(gatt, status, mtu))
    }

    override fun onServiceChanged(gatt: BluetoothGatt) {
        emitter?.onNext(RxGatt.ServiceChanged(gatt))
    }

    fun disconnect() {
        try {
            realGatt?.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun close() {
        try {
            realGatt?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            realGatt = null
        }
    }
}