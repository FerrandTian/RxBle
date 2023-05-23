/*
 * Copyright (C) TianFeng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tk.limt.utils

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import kotlin.math.ceil

fun BluetoothGattCharacteristic.isPropertySupported(property: Int) = property and properties != 0

val BluetoothGattCharacteristic.isNotificationSupported: Boolean
    get() = (isPropertySupported(BluetoothGattCharacteristic.PROPERTY_NOTIFY)
            || isPropertySupported(BluetoothGattCharacteristic.PROPERTY_INDICATE))
            && descriptors.isNotEmpty()

val BluetoothGattCharacteristic.isNotificationEnabled: Boolean
    get() = isNotificationSupported && descriptors[0]?.value?.let {
        !BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE.contentEquals(it)
    } ?: false

/**
 * Returns a string contains with each value in hexadecimal.
 *
 * @param withPrefix Determines whether a string starts with "0x".
 */
fun ByteArray?.hex(withPrefix: Boolean = false) = if (this?.isNotEmpty() == true) {
    val buf = StringBuilder(size * 2 + if (withPrefix) 2 else 0)
    if (withPrefix) buf.append("0x")
    for (b in this) buf.append(String.format("%02x", b.toInt() and 0xff))
    buf.toString()
} else ""

/**
 * Decodes a string from the bytes in UTF-8 encoding in this array.
 */
fun ByteArray?.string() = if (this?.isNotEmpty() == true) {
    decodeToString().trimEnd(Char.MIN_VALUE)
} else ""

/**
 * Splits this byte array to a list of arrays.
 *
 * @param limit The maximum size of sub-array to return.
 */
fun ByteArray.split(limit: Int): List<ByteArray> {
    require(limit > 0) { "limit should > 0" }
    val list = ArrayList<ByteArray>()
    var from = 0
    var to = Math.min(limit, size)
    do {
        list.add(copyOfRange(from, to))
        from += limit
        to = Math.min(from + limit, size)
    } while (from < size)
    return list
}

/**
 * Returns a byte array convert from a hexadecimal string.
 */
fun CharSequence.hexToBytes(): ByteArray {
    val bytes = ByteArray(ceil(length / 2f).toInt())
    var i = 0
    if (length % 2 != 0) {
        bytes[i] = get(i).digitToInt(16).toByte()
        i++
    }
    val builder = StringBuilder(2)
    while (i < length) {
        builder.clear()
        builder.append(get(i))
        if (i + 1 < length) builder.append(get(i + 1))
        bytes[ceil(i / 2f).toInt()] = builder.toString().toInt(16).toByte()
        i += 2
    }
    return bytes
}