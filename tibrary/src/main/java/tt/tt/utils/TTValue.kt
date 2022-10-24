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