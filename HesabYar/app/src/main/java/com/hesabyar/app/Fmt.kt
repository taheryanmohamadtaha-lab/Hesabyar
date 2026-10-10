package com.hesabyar.app

import java.util.Locale

object Fmt {
    private val faDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun fa(s: String): String {
        val sb = StringBuilder(s.length)
        for (ch in s) sb.append(if (ch in '0'..'9') faDigits[ch - '0'] else ch)
        return sb.toString()
    }

    fun fa(n: Int): String = fa(n.toString())

    fun num(n: Long): String = fa(String.format(Locale.US, "%,d", n))

    fun digitsOnly(s: String): String {
        val sb = StringBuilder()
        for (ch in s) {
            when (ch) {
                in '0'..'9' -> sb.append(ch)
                in '۰'..'۹' -> sb.append('0' + (ch - '۰'))
                in '٠'..'٩' -> sb.append('0' + (ch - '٠'))
                else -> {}
            }
        }
        return sb.toString()
    }

    fun disp(rial: Long, toman: Boolean): Long = if (toman) rial / 10 else rial
    fun unit(toman: Boolean): String = if (toman) "تومان" else "ریال"
    fun money(rial: Long, toman: Boolean): String = num(disp(rial, toman)) + " " + unit(toman)

    /** عدد با علامت، همیشه چپ‌به‌راست تا + و − جابه‌جا نشوند */
    fun signed(rial: Long, income: Boolean, toman: Boolean): String =
        "\u2066" + (if (income) "+" else "−") + num(disp(rial, toman)) + "\u2069 " + unit(toman)
}
