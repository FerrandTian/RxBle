package tk.limt.rxble.model

import tk.limt.utils.hex
import java.nio.ByteBuffer
import kotlin.math.min

internal class BytePack(val id: Int, val data: ByteArray) {
    val buffer: ByteBuffer = ByteBuffer.wrap(data)
    val hasRemaining: Boolean
        get() = buffer.hasRemaining()

    fun mark() {
        buffer.mark()
    }

    fun reset() {
        buffer.reset()
    }

    fun next(maxSize: Int): ByteArray {
        val array = ByteArray(min(maxSize, buffer.remaining()))
        buffer.get(array)
        return array
    }

    override fun toString(): String {
        return "BytePack(id=$id, data=${data.hex()})"
    }
}