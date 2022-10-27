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

@file:JvmName("TTValue")

package tt.tt.utils

import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * @author tianfeng
 */
fun ByteArray?.hex(withPrefix: Boolean = false) = if (this?.isNotEmpty() == true) {
    val buf = StringBuilder(size * 2 + if (withPrefix) 2 else 0)
    if (withPrefix) buf.append("0x")
    for (b in this) buf.append(String.format("%02x", b.toInt() and 0xff))
    buf.toString()
} else ""

fun ByteArray?.string() = if (this?.isNotEmpty() == true) {
    decodeToString().trimEnd(Char.MIN_VALUE)
} else ""

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

fun ByteArray.split(limit: Int): List<ByteArray> {
    require(limit > 0) { "limit should > 0" }
    val list = ArrayList<ByteArray>()
    var from = 0
    var to = kotlin.math.min(limit, size)
    do {
        list.add(copyOfRange(from, to))
        from += limit
        to = kotlin.math.min(from + limit, size)
    } while (from < size)
    return list
}