@file:JvmName("TTValue")

package tt.tt.utils

import android.location.Location

/**
 * @author tianfeng
 */
const val HEART_RATE_LOWER_LIMIT = 29
const val HEART_RATE_UPPER_LIMIT = 303

inline fun <R> notNull(vararg args: Any?, block: (args: Any?) -> R) =
    when {
        args.filterNotNull().size == args.size -> block(args)
        else -> null
    }

fun ipToBytes(ip: String): ByteArray {
    val bytes = ByteArray(4)
    val strings = ip.split(".")
    for (i in strings.indices) bytes[i] = strings[i].toInt().toByte()
    return bytes
}

fun ByteArray?.hex(withPrefix: Boolean = false) = if (this?.isNotEmpty() == true) {
    val buf = StringBuilder(size * 2 + if (withPrefix) 2 else 0)
    if (withPrefix) buf.append("0x")
    for (b in this) buf.append(String.format("%02x", b.toInt() and 0xff))
    buf.toString()
} else ""

fun ByteArray?.string() = if (this?.isNotEmpty() == true) {
    decodeToString().trimEnd(Char.MIN_VALUE)
} else ""

fun Location.isValid() = time > 0 &&
        latitude in -90.0..90.0 && latitude != 0.0 && longitude in -180.0..180.0 && longitude != 0.0

fun Location.notEqualTo(loc: Location) =
    latitude != loc.latitude || longitude != loc.longitude || altitude != loc.altitude

fun String.macAddress(): String {
    val sb = StringBuilder(this)
    val end = sb.length / 2
    for (i in 1 until end) {
        sb.insert(3 * i - 1, ":")
    }
    return sb.toString()
}

fun isHeartRateValid(heartRate: Int) = heartRate in HEART_RATE_LOWER_LIMIT..HEART_RATE_UPPER_LIMIT

fun calorie(heartRate: Int, durationInMin: Float, female: Boolean, age: Int, weight: Int): Float {
    val cal =
        if (female) (-20.44022f + 0.4472f * heartRate - 0.1263f * weight + 0.074f * age) / 4.184f * durationInMin
        else (-55.0969f + 0.6309f * heartRate + 0.1988f * weight + 0.2017f * age) / 4.184f * durationInMin
    return if (cal > 0) {
        if (heartRate > 90) cal else cal / 2f
    } else 0.0f
}

fun distance(
    startLatitude: Double,
    startLongitude: Double,
    endLatitude: Double,
    endLongitude: Double,
): Float {
    val result = floatArrayOf(0f, 0f, 0f)
    Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, result)
    return result[0]
}

fun distance(start: Location, end: Location): Float {
    val result = floatArrayOf(0f, 0f, 0f)
    Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, result)
    return result[0]
}

fun targetHeartRate(age: Int, restingHeartRate: Int, maxHeartRateInPercent: Float): Int =
    ((220 - age - restingHeartRate) * maxHeartRateInPercent + restingHeartRate).toInt()