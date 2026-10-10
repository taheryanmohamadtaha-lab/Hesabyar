package com.hesabyar.app

import androidx.compose.ui.graphics.Color

data class Tx(
    val id: Long = 0,
    val amount: Long,          // همیشه به ریال ذخیره می‌شود
    val isIncome: Boolean,
    val category: String,
    val note: String,
    val date: Long,
    val balance: Long? = null,
    val source: String = "manual"
)

object Cats {
    val expense = listOf(
        "خوراک", "حمل\u200cو\u200cنقل", "قبوض", "خرید", "سلامت", "تفریح",
        "انتقال", "شارژ و اینترنت", "خانه", "آموزش", "برداشت", "متفرقه"
    )
    val income = listOf("حقوق", "واریزی", "هدیه", "سود", "سایر")

    fun emoji(c: String): String = when (c) {
        "خوراک" -> "🍜"
        "حمل\u200cو\u200cنقل" -> "🚕"
        "قبوض" -> "💡"
        "خرید" -> "🛍️"
        "سلامت" -> "💊"
        "تفریح" -> "🎮"
        "انتقال" -> "🔁"
        "شارژ و اینترنت" -> "📱"
        "خانه" -> "🏠"
        "آموزش" -> "📚"
        "برداشت" -> "🏧"
        "متفرقه" -> "🧩"
        "حقوق" -> "💰"
        "واریزی" -> "📥"
        "هدیه" -> "🎁"
        "سود" -> "📈"
        "سایر" -> "✨"
        else -> "🌸"
    }

    private val palette = listOf(
        Color(0xFFFF7EB6), Color(0xFF9B6BFF), Color(0xFF6CC6FF), Color(0xFF2FD3A0),
        Color(0xFFFFB86B), Color(0xFFFF6B8B), Color(0xFFB38CFF), Color(0xFF5ED6E8),
        Color(0xFFFFD66B), Color(0xFF8FE388), Color(0xFFFF9E80), Color(0xFFA0A8FF)
    )

    fun color(c: String): Color {
        val all = expense + income
        val i = all.indexOf(c).let { if (it < 0) all.size else it }
        return palette[i % palette.size]
    }
}
