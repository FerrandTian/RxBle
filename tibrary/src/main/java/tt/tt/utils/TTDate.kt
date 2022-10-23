@file:JvmName("TTDate")

package tt.tt.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * @author tianfeng
 */
const val ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
const val yMdHmsTZD = "yyyy-MM-ddTHH:mm:ssTZD"
const val yMdHms = "yyyy-MM-dd HH:mm:ss"
const val yMdHms2 = "yyyy/MM/dd HH:mm:ss"
const val yMdHm = "yyyy-MM-dd HH:mm"
const val yMd = "yyyy-MM-dd"
const val yMd2 = "yyyy.MM.dd"
const val yMd3 = "yyyy/MM/dd"
const val yM = "yyyy-MM"
const val Md = "MM-dd"
const val MdHms = "MM-dd HH:mm:ss"
const val MdHm = "MM-dd HH:mm"
const val Hms = "HH:mm:ss"
const val Hmss = "HH:mm:ss.SSS"
const val Hm = "HH:mm"
const val ms = "mm:ss"

// 切割秒为天、小时、分钟、秒
fun splitSec(sec: Int): IntArray {
    val splits = IntArray(5)
    splits[0] = sec / 60 / 60 / 24
    splits[1] = sec / 60 / 60 % 24
    splits[2] = sec / 60 % 60
    splits[3] = sec % 60
    return splits
}

fun getDuration(strings: Array<String?>, sec: Int): String {
    val splits = splitSec(sec)
    val duration = StringBuilder()
    for (i in splits.indices) {
        if (splits[i] > 0) {
            duration.append(splits[i]).append(strings[i])
        }
    }
    return duration.toString()
}

fun getDuration(sec: Int): String {
    val hour: Int = sec / 3600
    val minute: Int = sec % 3600 / 60
    val second: Int = sec % 60
    return String.format("%02d:%02d:%02d", hour, minute, second)
}

fun getDurationInMin(durationInMin: Int): String {
    val hours = durationInMin / 60
    val minutes = durationInMin % 60
    return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
}

fun getDurationInSec(startTime: Long, endTime: Long): Int = ((endTime - startTime) / 1000L).toInt()

fun getAge(timestamp: Long): Int {
    var age = 0
    return try {
        val now = Calendar.getInstance()
        now.time = Date() // 当前时间
        val birthday = getCalendar(timestamp)
        if (!birthday.after(now)) { //如果传入的时间，在当前时间的后面，返回0岁
            age = now[Calendar.YEAR] - birthday[Calendar.YEAR] - 1
            if (now[Calendar.MONTH] > birthday[Calendar.MONTH]) {
                age++
            } else if (now[Calendar.MONTH] == birthday[Calendar.MONTH]
                && now[Calendar.DAY_OF_MONTH] >= birthday[Calendar.DAY_OF_MONTH]
            ) {
                age++
            }
        }
        age
    } catch (e: Exception) { // 兼容性更强,异常后返回数据
        0
    }
}

fun getPace(sec: Int): String {
    val minutes = sec / 60
    val seconds = sec % 60
    return String.format(Locale.getDefault(), "%02d'%02d\"", minutes, seconds)
}

fun getTimeZone(offsetMillis: Int): TimeZone {
    val zone = TimeZone.getTimeZone("GMT")
    zone.rawOffset = offsetMillis
    return zone
}

fun getTimeZoneName(offsetMillis: Int): String {
    val zone = TimeZone.getTimeZone("GMT")
    zone.rawOffset = offsetMillis
    return zone.getDisplayName(false, TimeZone.SHORT)
}

fun getCalendar(timestamp: Long): Calendar = getCalendar(timestamp, TimeZone.getDefault())

fun getCalendar(timestamp: Long, zone: TimeZone?): Calendar =
    getCalendar(timestamp, zone, Locale.getDefault())

fun getCalendar(timestamp: Long, zone: TimeZone?, locale: Locale?): Calendar {
    val calendar = Calendar.getInstance(zone, locale)
    calendar.timeInMillis = timestamp
    return calendar
}

// 根据规则，时区和地区，把时间戳转换为字符串
// 根据规则，把时间戳转换为字符串
// 根据规则和时区，把时间戳转换为字符串
@JvmOverloads
fun format(
    timestamp: Long,
    pattern: String?,
    zone: TimeZone? = TimeZone.getDefault(),
    locale: Locale? = Locale.getDefault(),
): String {
    var zone = zone
    var locale = locale
    if (zone == null) zone = TimeZone.getDefault()
    if (locale == null) locale = Locale.getDefault()
    val format = SimpleDateFormat(pattern, locale)
    format.timeZone = zone
    return format.format(Date(timestamp))
}

// 根据规则和时区，把字符串转换为时间戳
// 根据规则，把字符串转换为时间戳
@JvmOverloads
fun timestamp(datetime: String, pattern: String, locale: Locale = Locale.getDefault()): Long =
    SimpleDateFormat(pattern, locale).parse(datetime).time

@JvmOverloads
fun date(datetime: String, pattern: String, locale: Locale = Locale.getDefault()): Date =
    SimpleDateFormat(pattern, locale).parse(datetime)

// 获取一段时间（例如：10分钟）的(毫)秒数
fun getPeriod(field: Int, num: Long, inSec: Boolean): Long {
    var num = num
    when (field) {
        Calendar.YEAR -> {
            num *= 12
            num *= 30
            num *= 24
            num *= 60
            num *= 60
            if (!inSec) num *= 1000
        }
        Calendar.MONTH -> {
            num *= 30
            num *= 24
            num *= 60
            num *= 60
            if (!inSec) num *= 1000
        }
        Calendar.DAY_OF_MONTH -> {
            num *= 24
            num *= 60
            num *= 60
            if (!inSec) num *= 1000
        }
        Calendar.HOUR -> {
            num *= 60
            num *= 60
            if (!inSec) num *= 1000
        }
        Calendar.MINUTE -> {
            num *= 60
            if (!inSec) num *= 1000
        }
        Calendar.SECOND -> if (!inSec) num *= 1000
        Calendar.MILLISECOND -> {}
    }
    return num
}

private fun calendar(timestamp: Long): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    return calendar
}

fun year(timestamp: Long): Int = calendar(timestamp)[Calendar.YEAR]

fun month(timestamp: Long): Int = calendar(timestamp)[Calendar.MONTH] + 1

fun day(timestamp: Long): Int = calendar(timestamp)[Calendar.DAY_OF_MONTH]

fun hour(timestamp: Long): Int = calendar(timestamp)[Calendar.HOUR_OF_DAY]

fun minute(timestamp: Long): Int = calendar(timestamp)[Calendar.MINUTE]

fun second(timestamp: Long): Int = calendar(timestamp)[Calendar.SECOND]

fun millis(timestamp: Long): Int = calendar(timestamp)[Calendar.MILLISECOND]

fun isSameDay(date1: Date?, date2: Date?): Boolean {
    val calDateA = Calendar.getInstance()
    calDateA.time = date1
    val calDateB = Calendar.getInstance()
    calDateB.time = date2
    return calDateA[Calendar.YEAR] == calDateB[Calendar.YEAR] && calDateA[Calendar.MONTH] == calDateB[Calendar.MONTH] && calDateA[Calendar.DAY_OF_MONTH] == calDateB[Calendar.DAY_OF_MONTH]
}

fun getDayFromTimeDiff(d1: Date?, d2: Date?): Int =
    if (d1 == null || d2 == null) 0 else ((d2.time - d1.time) / (1000 * 3600 * 24)).toInt()

fun isSameMonth(date1: Date?, date2: Date?): Boolean {
    val calDateA = Calendar.getInstance()
    calDateA.time = date1
    val calDateB = Calendar.getInstance()
    calDateB.time = date2
    return (calDateA[Calendar.YEAR] == calDateB[Calendar.YEAR]
            && calDateA[Calendar.MONTH] == calDateB[Calendar.MONTH])
}