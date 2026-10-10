package com.hesabyar.app

import java.util.Calendar

object Jalali {
    data class D(val y: Int, val m: Int, val d: Int)

    val months = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
    private val weekdays = arrayOf(
        "", "یکشنبه", "دوشنبه", "سه\u200cشنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه"
    )

    fun of(ms: Long): D {
        val c = Calendar.getInstance()
        c.timeInMillis = ms
        return fromGregorian(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }

    fun fromGregorian(gy: Int, gm: Int, gd: Int): D {
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gdm[gm - 1]
        var jy = -1595 + 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + days / 31
            jd = 1 + days % 31
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + (days - 186) % 30
        }
        return D(jy, jm, jd)
    }

    fun toGregorian(jy0: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy = jy0 + 1595
        var days = -355668 + (365 * jy) + ((jy / 33) * 8) + (((jy % 33) + 3) / 4) + jd +
                (if (jm < 7) (jm - 1) * 31 else ((jm - 7) * 30) + 186)
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            days--
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        var gd = days + 1
        val leap = (gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)
        val sal = intArrayOf(0, 31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 13 && gd > sal[gm]) {
            gd -= sal[gm]
            gm++
        }
        return Triple(gy, gm, gd)
    }

    fun monthStart(y: Int, m: Int): Long {
        val g = toGregorian(y, m, 1)
        val c = Calendar.getInstance()
        c.clear()
        c.set(g.first, g.second - 1, g.third, 0, 0, 0)
        return c.timeInMillis
    }

    fun monthOffset(y: Int, m: Int, delta: Int): Pair<Int, Int> {
        val idx = y * 12 + (m - 1) + delta
        return Pair(idx / 12, idx % 12 + 1)
    }

    fun daysInMonth(y: Int, m: Int): Int {
        val n = monthOffset(y, m, 1)
        val diff = monthStart(n.first, n.second) - monthStart(y, m)
        return Math.round(diff / 86_400_000.0).toInt()
    }

    fun dayStart(ms: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = ms
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun addDays(ms: Long, n: Int): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = ms
        c.add(Calendar.DAY_OF_YEAR, n)
        return c.timeInMillis
    }

    /** هفته ایرانی از شنبه شروع می‌شود */
    fun weekStart(ms: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = dayStart(ms)
        while (c.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) c.add(Calendar.DAY_OF_YEAR, -1)
        return c.timeInMillis
    }

    fun dateText(ms: Long): String {
        val j = of(ms)
        val c = Calendar.getInstance()
        c.timeInMillis = ms
        return weekdays[c.get(Calendar.DAY_OF_WEEK)] + " " + Fmt.fa(j.d) + " " + months[j.m - 1] + " " + Fmt.fa(j.y)
    }

    fun shortDay(ms: Long): String {
        val j = of(ms)
        return Fmt.fa(j.d) + " " + months[j.m - 1]
    }

    fun fullNumeric(ms: Long): String {
        val j = of(ms)
        return "%04d/%02d/%02d".format(j.y, j.m, j.d)
    }

    fun timeText(ms: Long): String {
        val c = Calendar.getInstance()
        c.timeInMillis = ms
        return Fmt.fa("%02d:%02d".format(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)))
    }
}
