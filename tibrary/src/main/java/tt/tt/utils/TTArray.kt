@file:JvmName("TTArray")

package tt.tt.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author tianfeng
 */

fun toBytes(`val`: Int): ByteArray? {
    val buffer = ByteBuffer.allocate(Int.SIZE_BYTES).order(ByteOrder.nativeOrder())
    buffer.putInt(`val`)
    return buffer.array()
}

fun toBytes(values: IntArray): ByteArray? {
    val buffer = ByteBuffer.allocate(Int.SIZE_BYTES * values.size).order(ByteOrder.nativeOrder())
    for (`val` in values) {
        buffer.putInt(`val`)
    }
    return buffer.array()
}

fun scale(rate: Float, array: DoubleArray): DoubleArray {
    require(rate > 0) { "Scale rate must > 0" }
    require(array.size > 1) { "Array length must > 1" }
    val length = (array.size * rate).toInt()
    val dest = DoubleArray(length)
    when {
        length == array.size -> System.arraycopy(array, 0, dest, 0, length)
        rate > 1 -> {
            var start = 0
            var last = array[start]
            for (i in 1 until array.size) {
                val len = ((i + 1) * rate).toInt() - start
                if (len > 1) {
                    val step = (array[i] - last) / (len - 1)
                    for (j in 0 until len) dest[start + j] = last + step * j
                } else dest[start] = array[i]
                start += len
                last = array[i]
            }
        }
        else -> for (i in 0 until length) dest[i] = array[(i / rate).toInt()]
    }
    return dest
}

fun getSkip(duration: Int) = duration / 900

fun scale(rate: Float, array: IntArray): IntArray {
    require(rate > 0) { "Scale rate must > 0" }
    require(array.size > 1) { "Array length must > 1" }
    val length = (array.size * rate).toInt()
    val dest = IntArray(length)
    when (length) {
        array.size -> System.arraycopy(array, 0, dest, 0, length)
        else -> if (rate > 1) {
            var start = 0
            var last = array[start]
            for (i in 1 until array.size) {
                val len = ((i + 1) * rate).toInt() - start
                if (len > 1) {
                    val step = 1.0f * (array[i] - last) / (len - 1)
                    for (j in 0 until len) dest[start + j] = (last + step * j).toInt()
                } else dest[start] = array[i]
                start += len
                last = array[i]
            }
        } else for (i in 0 until length) dest[i] = array[(i / rate).toInt()]
    }
    return dest
}

fun IntArray.medIndex(): IntArray {
    if (isEmpty()) return IntArray(0)
    val sorted = copyOf().apply { sort() }
    val med = sorted[sorted.size / 2]
    val index = ArrayList<Int>()
    for (i in indices) if (this[i] == med) index.add(i)
    return index.toIntArray()
}