package id.ac.kuliah.laporaja.core.time

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object AppTime {

    private val monthsShort = listOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des",
    )
    private val dayNames = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")

    private const val SECOND = 1_000L
    private const val MINUTE = 60 * SECOND
    private const val HOUR = 60 * MINUTE
    private const val DAY = 24 * HOUR

    private val fractionalPattern = Regex("\\.\\d+")
    private val spaceSeparatorPattern = Regex("^\\d{4}-\\d{2}-\\d{2} ")

    fun parse(source: String?): Long {
        if (source.isNullOrBlank()) return 0L
        var text = source.trim()
        if (spaceSeparatorPattern.matches(text) && text.length > 10) {
            text = text.substring(0, 10) + "T" + text.substring(11)
        }
        text = text.replace(fractionalPattern, "")
        parseWith("yyyy-MM-dd'T'HH:mm:ssXXX", text)?.let { return it }
        parseWith("yyyy-MM-dd'T'HH:mm:ss", text)?.let { return it }
        parseWith("yyyy-MM-dd HH:mm:ss", text)?.let { return it }
        return 0L
    }

    fun toIsoUtc(epochMillis: Long): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(epochMillis))

    fun now(): Long = System.currentTimeMillis()

    fun dateShort(epochMillis: Long): String {
        if (epochMillis <= 0L) return "-"
        val calendar = calendarOf(epochMillis)
        return "${calendar.get(Calendar.DAY_OF_MONTH)} ${monthsShort[calendar.get(Calendar.MONTH)]} ${calendar.get(Calendar.YEAR)}"
    }

    fun dateTime(epochMillis: Long): String {
        if (epochMillis <= 0L) return "-"
        return "${dateShort(epochMillis)} ${timeOfDay(epochMillis)}"
    }

    fun dateTimeLong(epochMillis: Long): String {
        if (epochMillis <= 0L) return "-"
        val calendar = calendarOf(epochMillis)
        return "${dayNames[calendar.get(Calendar.DAY_OF_WEEK) - 1]}, ${dateShort(epochMillis)} ${timeOfDay(epochMillis)}"
    }

    fun timeOfDay(epochMillis: Long): String {
        val calendar = calendarOf(epochMillis)
        return "%02d.%02d".format(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    }

    fun relative(epochMillis: Long, nowMillis: Long = now()): String {
        if (epochMillis <= 0L) return "-"
        val diff = nowMillis - epochMillis
        if (diff < 0L) return dateTime(epochMillis)
        return when {
            diff < MINUTE -> "Baru saja"
            diff < HOUR -> "${diff / MINUTE} menit lalu"
            diff < DAY -> "${diff / HOUR} jam lalu"
            diff < 7 * DAY -> "${diff / DAY} hari lalu"
            else -> dateShort(epochMillis)
        }
    }

    fun startOfToday(epochMillis: Long = now()): Long = calendarOf(epochMillis).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun parseWith(pattern: String, text: String): Long? = try {
        val formatter = SimpleDateFormat(pattern, Locale.US)
        if (pattern.endsWith("'Z'") || !pattern.contains("X")) {
            formatter.timeZone = TimeZone.getTimeZone("UTC")
        }
        formatter.parse(text)?.time
    } catch (_: Exception) {
        null
    }

    private fun calendarOf(epochMillis: Long): Calendar =
        Calendar.getInstance(TimeZone.getDefault()).apply { timeInMillis = epochMillis }
}
