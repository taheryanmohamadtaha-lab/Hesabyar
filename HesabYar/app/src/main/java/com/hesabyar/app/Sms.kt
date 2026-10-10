package com.hesabyar.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import java.security.MessageDigest

/** تحلیل متن پیامک بانکی (بانک ملت و بانک‌های مشابه) */
object SmsParser {
    data class Result(val amount: Long, val income: Boolean, val balance: Long?, val category: String)

    private val incomeKw = listOf("واریز", "دریافت", "سپرده", "حقوق", "برگشت", "بازگشت", "سود")
    private val expenseKw = listOf(
        "برداشت", "خرید", "پرداخت", "انتقال", "کارت به کارت", "قبض", "کسر", "پایا", "ساتنا", "شارژ", "چک"
    )
    private val blockWords = listOf("رمز", "کد تایید", "کد تأیید", "کد فعال")

    fun norm(s: String): String {
        val sb = StringBuilder(s.length)
        for (ch in s) {
            when (ch) {
                in '۰'..'۹' -> sb.append('0' + (ch - '۰'))
                in '٠'..'٩' -> sb.append('0' + (ch - '٠'))
                'ي' -> sb.append('ی')
                'ك' -> sb.append('ک')
                '،', '٬' -> sb.append(',')
                '٫' -> sb.append('.')
                '\u200c' -> sb.append(' ')
                else -> sb.append(ch)
            }
        }
        return sb.toString().replace(Regex("(?<=\\d),(?=\\d)"), "")
    }

    private fun firstIndex(t: String, words: List<String>): Int? =
        words.map { t.indexOf(it) }.filter { it >= 0 }.minOrNull()

    fun parse(sender: String?, body: String, loose: Boolean = false): Result? {
        val t = norm(body)
        if (blockWords.any { t.contains(it) }) return null

        // مانده حساب
        val bm = Regex("""(?:مانده|موجودی)\s*(?:حساب|قابل برداشت)?\s*[:：]?\s*(-?\d+)""").find(t)
        val balance = bm?.groupValues?.get(1)?.toLongOrNull()
        val tx = if (bm != null) t.removeRange(bm.range) else t

        var amount: Long? = null
        var income: Boolean? = null

        // ۱) «مبلغ ...»
        val m0 = Regex("""مبلغ\s*[:：]?\s*([+\-]?)(\d+)([+\-]?)""").find(tx)
        if (m0 != null) {
            amount = m0.groupValues[2].toLongOrNull()
            val s = m0.groupValues[1] + m0.groupValues[3]
            income = if (s.contains("+")) true else if (s.contains("-")) false else null
        }

        // ۲) کلمه کلیدی + عدد
        if (amount == null) {
            val kw = (incomeKw + expenseKw).joinToString("|") { Regex.escape(it) }
            val m1 = Regex("($kw)[^\\d\\n]{0,14}?(\\d+)").find(tx)
            if (m1 != null) {
                amount = m1.groupValues[2].toLongOrNull()
                income = incomeKw.contains(m1.groupValues[1])
            }
        }

        // ۳) عدد علامت‌دار مثل 500000- یا +500000
        if (amount == null) {
            val s1 = Regex("""(?:^|\s)([+\-])(\d{4,})""").find(tx)
            val s2 = Regex("""(\d{4,})([+\-])(?=\s|$)""").find(tx)
            if (s1 != null) {
                amount = s1.groupValues[2].toLongOrNull()
                income = s1.groupValues[1] == "+"
            } else if (s2 != null) {
                amount = s2.groupValues[1].toLongOrNull()
                income = s2.groupValues[2] == "+"
            }
        }

        val amt = amount ?: return null
        if (amt <= 0L) return null

        val senderStr = sender ?: ""
        val bankish = balance != null || t.contains("ملت") ||
                senderStr.contains("mellat", ignoreCase = true) || senderStr.contains("ملت")
        if (!bankish && !loose) return null

        val inc: Boolean = income ?: run {
            val fi = firstIndex(t, incomeKw)
            val fe = firstIndex(t, expenseKw)
            fi != null && (fe == null || fi < fe)
        }

        val cat = if (inc) {
            when {
                t.contains("حقوق") -> "حقوق"
                t.contains("سود") -> "سود"
                else -> "واریزی"
            }
        } else {
            when {
                t.contains("قبض") -> "قبوض"
                t.contains("شارژ") || t.contains("اینترنت") -> "شارژ و اینترنت"
                t.contains("کارت به کارت") || t.contains("انتقال") || t.contains("پایا") || t.contains("ساتنا") -> "انتقال"
                t.contains("خرید") -> "خرید"
                t.contains("برداشت") -> "برداشت"
                else -> "متفرقه"
            }
        }
        return Result(amt, inc, balance, cat)
    }
}

object SmsImporter {
    private fun sha(s: String): String =
        MessageDigest.getInstance("SHA-1").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }

    fun handle(ctx: Context, sender: String?, body: String, date: Long): Boolean {
        val r = SmsParser.parse(sender, body, Prefs.loose(ctx)) ?: return false
        val hash = sha(SmsParser.norm(body).trim())
        if (Repo.db.exists(hash, date)) return false
        val note = body.trim().replace(Regex("\\s*\\n+\\s*"), " | ").take(160)
        Repo.db.insert(Tx(0, r.amount, r.income, r.category, note, date, r.balance, "sms"), hash)
        return true
    }

    fun importInbox(ctx: Context): Int {
        var n = 0
        val c = ctx.contentResolver.query(
            Uri.parse("content://sms/inbox"),
            arrayOf("address", "body", "date"),
            null, null, "date DESC"
        ) ?: return 0
        c.use {
            while (it.moveToNext()) {
                val body = it.getString(1) ?: continue
                if (handle(ctx, it.getString(0), body, it.getLong(2))) n++
            }
        }
        if (n > 0) Repo.reload()
        return n
    }
}

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        Repo.init(context)
        val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (msgs.isEmpty()) return
        val sender = msgs[0].originatingAddress
        val body = msgs.joinToString("") { it.messageBody ?: "" }
        val ts = msgs[0].timestampMillis
        val date = if (ts > 0) ts else System.currentTimeMillis()
        if (SmsImporter.handle(context, sender, body, date)) Repo.reload()
    }
}
