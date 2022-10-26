/*
 * Copyright (c) 2022-present, TianFeng.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package tk.limt.utils

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

fun BluetoothGattCharacteristic.support(property: Int) = property and properties != 0

val BluetoothGattCharacteristic.supportNotification: Boolean
    get() = (support(BluetoothGattCharacteristic.PROPERTY_NOTIFY) || support(
        BluetoothGattCharacteristic.PROPERTY_INDICATE
    )) && descriptors.isNotEmpty()

val BluetoothGattCharacteristic.notificationEnabled: Boolean
    get() = supportNotification && descriptors[0].value != null && !BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE.contentEquals(
        descriptors[0].value
    )