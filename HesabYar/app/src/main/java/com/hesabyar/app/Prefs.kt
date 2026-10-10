package com.hesabyar.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("hesab", Context.MODE_PRIVATE)

    fun toman(c: Context): Boolean = sp(c).getBoolean("toman", true)
    fun setToman(c: Context, v: Boolean) = sp(c).edit().putBoolean("toman", v).apply()

    /** بودجه ماهانه به ریال */
    fun budget(c: Context): Long = sp(c).getLong("budget", 0L)
    fun setBudget(c: Context, v: Long) = sp(c).edit().putLong("budget", v).apply()

    fun loose(c: Context): Boolean = sp(c).getBoolean("loose", false)
    fun setLoose(c: Context, v: Boolean) = sp(c).edit().putBoolean("loose", v).apply()
}

fun toast(ctx: Context, msg: String) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(ctx.applicationContext, msg, Toast.LENGTH_LONG).show()
    }
}
