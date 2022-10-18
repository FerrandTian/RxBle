package tk.limt.utils

fun ByteArray.hex() = if (this.isEmpty()) "" else {
    val buf = StringBuilder(size * 2)
    for (b in this) buf.append(String.format("%02x", b.toInt() and 0xff))
    buf.toString()
}

fun ByteArray.string() = this.decodeToString().trimEnd(Char.MIN_VALUE)