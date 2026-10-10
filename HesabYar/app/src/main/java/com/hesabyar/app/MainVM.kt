package com.hesabyar.app

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainVM(app: Application) : AndroidViewModel(app) {
    val txs = Repo.txs
    val toman = MutableStateFlow(Prefs.toman(app))
    val budget = MutableStateFlow(Prefs.budget(app))
    val loose = MutableStateFlow(Prefs.loose(app))

    private val ctx: Application get() = getApplication()

    fun setToman(v: Boolean) { Prefs.setToman(ctx, v); toman.value = v }
    fun setBudget(rial: Long) { Prefs.setBudget(ctx, rial); budget.value = rial }
    fun setLoose(v: Boolean) { Prefs.setLoose(ctx, v); loose.value = v }

    fun importSms(silent: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val n = try { SmsImporter.importInbox(ctx) } catch (e: Exception) { -1 }
            if (n > 0) toast(ctx, Fmt.fa(n) + " تراکنش جدید از پیامک‌ها وارد شد 🎉")
            else if (!silent) toast(ctx, if (n == 0) "پیامک بانکی جدیدی پیدا نشد" else "خواندن پیامک‌ها با خطا روبه‌رو شد")
        }
    }

    fun add(t: Tx) { viewModelScope.launch(Dispatchers.IO) { Repo.db.insert(t); Repo.reload() } }
    fun update(t: Tx) { viewModelScope.launch(Dispatchers.IO) { Repo.db.update(t); Repo.reload() } }
    fun delete(id: Long) { viewModelScope.launch(Dispatchers.IO) { Repo.db.delete(id); Repo.reload() } }
    fun clearAll() { viewModelScope.launch(Dispatchers.IO) { Repo.db.deleteAll(); Repo.reload() } }

    fun exportCsv(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sb = StringBuilder("\uFEFF")
                sb.append("تاریخ شمسی,ساعت,نوع,مبلغ (ریال),دسته,توضیح,مانده (ریال)\n")
                for (t in Repo.txs.value) {
                    val note = t.note.replace("\"", "\"\"")
                    sb.append(Jalali.fullNumeric(t.date)).append(',')
                        .append(Jalali.timeText(t.date).let { Fmt.digitsOnly(it).let { d -> d.take(2) + ":" + d.drop(2) } }).append(',')
                        .append(if (t.isIncome) "درآمد" else "هزینه").append(',')
                        .append(t.amount).append(',')
                        .append(t.category).append(',')
                        .append('"').append(note).append('"').append(',')
                        .append(t.balance?.toString() ?: "").append('\n')
                }
                ctx.contentResolver.openOutputStream(uri)?.use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }
                toast(ctx, "فایل CSV ذخیره شد ✅")
            } catch (e: Exception) {
                toast(ctx, "ذخیره فایل ناموفق بود")
            }
        }
    }
}
