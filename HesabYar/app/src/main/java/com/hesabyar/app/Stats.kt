package com.hesabyar.app

data class Sum(val income: Long, val expense: Long) {
    val net: Long get() = income - expense
}

class Period(val label: String, val from: Long, val to: Long)

object Stats {
    fun sum(list: List<Tx>, from: Long, to: Long): Sum {
        var i = 0L
        var e = 0L
        for (t in list) {
            if (t.date >= from && t.date < to) {
                if (t.isIncome) i += t.amount else e += t.amount
            }
        }
        return Sum(i, e)
    }

    fun byCategory(list: List<Tx>, from: Long, to: Long): List<Pair<String, Long>> =
        list.filter { !it.isIncome && it.date >= from && it.date < to }
            .groupBy { it.category }
            .map { entry -> entry.key to entry.value.sumOf { x -> x.amount } }
            .sortedByDescending { it.second }

    fun weekPeriods(now: Long, n: Int): List<Period> {
        val ws = Jalali.weekStart(now)
        return (n - 1 downTo 0).map { i ->
            val from = Jalali.addDays(ws, -7 * i)
            val to = Jalali.addDays(from, 7)
            Period(Jalali.shortDay(from), from, to)
        }
    }

    fun monthPeriods(now: Long, n: Int): List<Period> {
        val j = Jalali.of(now)
        return (n - 1 downTo 0).map { i ->
            val a = Jalali.monthOffset(j.y, j.m, -i)
            val b = Jalali.monthOffset(a.first, a.second, 1)
            Period(Jalali.months[a.second - 1], Jalali.monthStart(a.first, a.second), Jalali.monthStart(b.first, b.second))
        }
    }
}
