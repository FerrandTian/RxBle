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

internal class RxBleOnSubscribe(
    private val ctx: Context,
    private val device: BluetoothDevice,
    var autoConnect: Boolean = false,
) : ObservableOnSubscribe<RxGatt> {
    var realGatt: BluetoothGatt? = null
        private set
    val gatt: BluetoothGatt
        get() {
            requireNotNull(realGatt) { "GATT client has not been established or has been closed" }
            return realGatt!!
        }
    var connectionState: Int = BluetoothProfile.STATE_DISCONNECTED
        private set
    var mtu: Int = 20
        private set

    override fun subscribe(emitter: ObservableEmitter<RxGatt>) {
        connectionState = BluetoothProfile.STATE_CONNECTING
        realGatt = device.connectGatt(ctx, autoConnect, object : BluetoothGattCallback() {
            override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
                emitter.onNext(RxGatt.PhyUpdate(gatt, Phy(txPhy, rxPhy), status))
            }

            override fun onPhyRead(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
                emitter.onNext(RxGatt.PhyRead(gatt, Phy(txPhy, rxPhy), status))
            }

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                connectionState = newState
                emitter.onNext(RxGatt.ConnectionStateChange(gatt, status, newState))
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                emitter.onNext(RxGatt.ServicesDiscovered(gatt, status))
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int,
            ) {
                emitter.onNext(RxGatt.CharacteristicRead(gatt, characteristic, status))
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int,
            ) {
                emitter.onNext(RxGatt.CharacteristicWrite(gatt, characteristic, status))
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
            ) {
                emitter.onNext(RxGatt.CharacteristicChanged(gatt, characteristic))
            }

            override fun onDescriptorRead(
                gatt: BluetoothGatt,
                descriptor: BluetoothGattDescriptor,
                status: Int,
            ) {
                emitter.onNext(RxGatt.DescriptorRead(gatt, descriptor, status))
            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt,
                descriptor: BluetoothGattDescriptor,
                status: Int,
            ) {
                emitter.onNext(RxGatt.DescriptorWrite(gatt, descriptor, status))
            }

            override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
                emitter.onNext(RxGatt.ReliableWriteCompleted(gatt, status))
            }

            override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
                emitter.onNext(RxGatt.ReadRemoteRssi(gatt, rssi, status))
            }

            override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                this@RxBleOnSubscribe.mtu = mtu - 3
                emitter.onNext(RxGatt.MtuChanged(gatt, mtu, status))
            }

            override fun onServiceChanged(gatt: BluetoothGatt) {
                emitter.onNext(RxGatt.ServiceChanged(gatt))
            }
        }, BluetoothDevice.TRANSPORT_LE)
        emitter.setCancellable {
            connectionState = BluetoothProfile.STATE_DISCONNECTED
            realGatt?.close()
            realGatt = null
        }
    }
}