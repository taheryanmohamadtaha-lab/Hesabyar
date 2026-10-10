package com.hesabyar.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

// ───────────────────────── خانه ─────────────────────────
@Composable
fun HomeScreen(vm: MainVM, hasPerm: Boolean, onAskPerm: () -> Unit) {
    val txs by vm.txs.collectAsState()
    val toman by vm.toman.collectAsState()
    val budget by vm.budget.collectAsState()

    val now = System.currentTimeMillis()
    val j = Jalali.of(now)
    val mStart = Jalali.monthStart(j.y, j.m)
    val nxt = Jalali.monthOffset(j.y, j.m, 1)
    val mEnd = Jalali.monthStart(nxt.first, nxt.second)
    val sum = Stats.sum(txs, mStart, mEnd)
    val lastBal = txs.firstOrNull { it.balance != null }?.balance
    val dim = Jalali.daysInMonth(j.y, j.m)
    val avg = sum.expense / maxOf(1, j.d)
    val projected = avg * dim
    val ratio = if (budget > 0) sum.expense.toFloat() / budget else 0f

    val mood = when {
        txs.isEmpty() -> 0
        budget > 0 -> if (ratio < 0.7f) 0 else if (ratio < 1f) 1 else 2
        sum.income > 0 && sum.expense > sum.income -> 2
        sum.expense > sum.income -> 1
        else -> 0
    }
    val bubble = when {
        txs.isEmpty() -> "سلام! من حساب‌یارم ✨ پیامک‌های بانکی‌ت رو وارد کن تا با هم حساب‌کتاب کنیم."
        budget <= 0 -> "برای اینکه بهتر کمکت کنم، از «تنظیمات» یه بودجه ماهانه تعیین کن 💖"
        mood == 0 -> "عالیه! هنوز خیلی تا سقف بودجه فاصله داری، همینجوری ادامه بده ✨"
        mood == 1 -> "حواست به خرج‌ها باشه، داری به سقف بودجه نزدیک می‌شی 👀"
        else -> "اوه نه! از بودجه این ماه رد شدی 😢 بیا بقیه ماه رو با احتیاط خرج کنیم."
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Mascot(mood, Modifier.size(110.dp))
            Spacer(Modifier.width(8.dp))
            Glass(Modifier.weight(1f)) { Txt(bubble, 14, FontWeight.Bold) }
        }

        if (!hasPerm) {
            Glass(Modifier.fillMaxWidth()) {
                Txt("📩 اجازه خواندن پیامک", 15, FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                Txt("برای ثبت خودکار درآمد و هزینه، برنامه باید پیامک‌های بانکی رو بخونه. هیچ اطلاعاتی از گوشی بیرون نمی‌ره.", 12, color = Pal.soft)
                Spacer(Modifier.height(10.dp))
                PillButton("اجازه می‌دم و وارد کن", onClick = onAskPerm)
            }
        }

        Glass(Modifier.fillMaxWidth()) {
            Txt("مانده حساب (طبق آخرین پیامک)", 12, color = Pal.soft)
            Txt(if (lastBal != null) Fmt.money(lastBal, toman) else "—", 26, FontWeight.ExtraBold)
            Txt("ماه جاری: " + Jalali.months[j.m - 1] + " " + Fmt.fa(j.y), 12, color = Pal.soft)
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Glass(Modifier.weight(1f)) {
                Txt("📥 درآمد این ماه", 12, color = Pal.soft)
                Txt(Fmt.money(sum.income, toman), 15, FontWeight.ExtraBold, Pal.mint)
            }
            Glass(Modifier.weight(1f)) {
                Txt("📤 هزینه این ماه", 12, color = Pal.soft)
                Txt(Fmt.money(sum.expense, toman), 15, FontWeight.ExtraBold, Pal.coral)
            }
        }

        if (budget > 0) {
            Glass(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Txt("🎯 بودجه ماهانه", 14, FontWeight.Bold, modifier = Modifier.weight(1f))
                    Txt(Fmt.fa((ratio * 100).toInt()) + "٪", 14, FontWeight.ExtraBold, if (ratio >= 1f) Pal.coral else Pal.violet)
                }
                Spacer(Modifier.height(8.dp))
                Progress(ratio, if (ratio >= 1f) Pal.warn else Pal.grad)
                Spacer(Modifier.height(8.dp))
                Txt(
                    if (ratio < 1f) "باقی‌مانده: " + Fmt.money(budget - sum.expense, toman)
                    else "از بودجه " + Fmt.money(sum.expense - budget, toman) + " بیشتر خرج کردی",
                    12, color = Pal.soft
                )
            }
        }

        // دونات دسته‌ها
        val cats = Stats.byCategory(txs, mStart, mEnd)
        Glass(Modifier.fillMaxWidth()) {
            Txt("🍩 هزینه‌ها به تفکیک دسته", 14, FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            if (cats.isEmpty()) {
                Txt("هنوز هزینه‌ای در این ماه ثبت نشده.", 12, color = Pal.soft)
            } else {
                val top = cats.take(6)
                val rest = cats.drop(6).sumOf { it.second }
                val slices = top.map { Pair(Cats.color(it.first), it.second.toFloat()) } +
                        (if (rest > 0) listOf(Pair(Color(0xFFB8B0D0), rest.toFloat())) else emptyList())
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Donut(slices, Modifier.size(170.dp)) { Txt(Cats.emoji(top.first().first), 34) }
                }
                Spacer(Modifier.height(12.dp))
                val total = maxOf(1L, sum.expense)
                for (c in top) {
                    LegendRow(Cats.color(c.first), Cats.emoji(c.first) + " " + c.first, c.second, total, toman)
                }
                if (rest > 0) LegendRow(Color(0xFFB8B0D0), "🌀 سایر", rest, total, toman)
            }
        }

        // نکته‌ها
        val biggest = txs.filter { !it.isIncome && it.date >= mStart && it.date < mEnd }.maxByOrNull { it.amount }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Glass(Modifier.weight(1f)) {
                Txt("☀️ میانگین هزینه روزانه", 12, color = Pal.soft)
                Txt(Fmt.money(avg, toman), 14, FontWeight.ExtraBold)
            }
            Glass(Modifier.weight(1f)) {
                Txt("🔮 پیش‌بینی هزینه پایان ماه", 12, color = Pal.soft)
                Txt(Fmt.money(projected, toman), 14, FontWeight.ExtraBold)
            }
        }
        if (biggest != null) {
            Glass(Modifier.fillMaxWidth()) {
                Txt("💸 بزرگ‌ترین هزینه این ماه", 12, color = Pal.soft)
                Txt(Cats.emoji(biggest.category) + " " + biggest.category + " — " + Fmt.money(biggest.amount, toman), 14, FontWeight.ExtraBold)
                Txt(Jalali.dateText(biggest.date), 11, color = Pal.soft)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LegendRow(color: Color, name: String, value: Long, total: Long, toman: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Txt(name, 13, modifier = Modifier.weight(1f))
        Txt(Fmt.money(value, toman), 12, FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Txt(Fmt.fa((value * 100.0 / total).roundToInt()) + "٪", 12, color = Pal.soft)
    }
}

// ───────────────────────── تراکنش‌ها ─────────────────────────
@Composable
fun TxScreen(vm: MainVM) {
    val txs by vm.txs.collectAsState()
    val toman by vm.toman.collectAsState()
    var filter by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Tx?>(null) }
    var adding by remember { mutableStateOf(false) }

    val list = txs.filter {
        (filter == 0 || (filter == 1 && it.isIncome) || (filter == 2 && !it.isIncome)) &&
                (query.isBlank() || it.note.contains(query) || it.category.contains(query))
    }
    val groups = list.groupBy { Jalali.dayStart(it.date) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Chip("همه", filter == 0) { filter = 0 }
                Chip("📥 درآمد", filter == 1) { filter = 1 }
                Chip("📤 هزینه", filter == 2) { filter = 2 }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Txt("🔎 جستجو در دسته یا توضیح…", 13, color = Pal.soft) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            if (list.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Txt("هنوز تراکنشی نیست 🌸\nبا دکمه ➕ اضافه کن یا از تنظیمات پیامک‌ها رو وارد کن", 13, color = Pal.soft, align = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for ((day, rows) in groups) {
                        item(key = "h$day") {
                            Txt(Jalali.dateText(day), 12, FontWeight.Bold, Pal.violet, Modifier.padding(top = 8.dp))
                        }
                        items(rows, key = { it.id }) { t -> TxRow(t, toman) { editing = t } }
                    }
                }
            }
        }
        Box(
            Modifier.align(Alignment.BottomStart).padding(20.dp)
                .size(60.dp).clip(CircleShape).background(Pal.grad)
                .clickable { adding = true },
            contentAlignment = Alignment.Center
        ) { Txt("➕", 24) }
    }

    if (adding) {
        TxDialog(null, toman, onDismiss = { adding = false }, onDelete = null) { vm.add(it); adding = false }
    }
    editing?.let { t ->
        TxDialog(t, toman, onDismiss = { editing = null }, onDelete = { vm.delete(t.id); editing = null }) {
            vm.update(it); editing = null
        }
    }
}

@Composable
private fun TxRow(t: Tx, toman: Boolean, onClick: () -> Unit) {
    Glass(Modifier.fillMaxWidth(), pad = 12.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(Cats.color(t.category).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) { Txt(Cats.emoji(t.category), 20) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Txt(t.category + if (t.source == "sms") "  📩" else "", 14, FontWeight.Bold)
                if (t.note.isNotBlank()) Txt(t.note, 11, color = Pal.soft, maxLines = 1)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Txt(Fmt.signed(t.amount, t.isIncome, toman), 13, FontWeight.ExtraBold, if (t.isIncome) Pal.mint else Pal.coral)
                Txt(Jalali.timeText(t.date), 10, color = Pal.soft)
            }
        }
    }
}

@Composable
fun TxDialog(initial: Tx?, toman: Boolean, onDismiss: () -> Unit, onDelete: (() -> Unit)?, onSave: (Tx) -> Unit) {
    var income by remember { mutableStateOf(initial?.isIncome ?: false) }
    var amount by remember { mutableStateOf(if (initial != null) Fmt.disp(initial.amount, toman).toString() else "") }
    var cat by remember { mutableStateOf(initial?.category ?: "متفرقه") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var daysAgo by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp),
        title = { Txt(if (initial == null) "تراکنش جدید ✨" else "ویرایش تراکنش", 18, FontWeight.ExtraBold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("📤 هزینه", !income) { income = false; if (cat !in Cats.expense) cat = "متفرقه" }
                    Chip("📥 درآمد", income) { income = true; if (cat !in Cats.income) cat = "سایر" }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = Fmt.digitsOnly(it) },
                    label = { Txt("مبلغ (" + Fmt.unit(toman) + ")", 12, color = Pal.soft) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (amount.isNotEmpty()) {
                    Txt(Fmt.num(amount.toLongOrNull() ?: 0L) + " " + Fmt.unit(toman), 12, color = Pal.violet)
                }
                Txt("دسته", 12, color = Pal.soft)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (c in if (income) Cats.income else Cats.expense) {
                        Chip(Cats.emoji(c) + " " + c, cat == c) { cat = c }
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Txt("توضیح (اختیاری)", 12, color = Pal.soft) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (initial == null) {
                    Txt("تاریخ", 12, color = Pal.soft)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Chip("امروز", daysAgo == 0) { daysAgo = 0 }
                        Chip("دیروز", daysAgo == 1) { daysAgo = 1 }
                        Chip("پریروز", daysAgo == 2) { daysAgo = 2 }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val v = amount.toLongOrNull() ?: 0L
                if (v > 0L) {
                    val rial = if (toman) v * 10 else v
                    val base = initial ?: Tx(
                        amount = rial, isIncome = income, category = cat, note = note,
                        date = Jalali.addDays(System.currentTimeMillis(), -daysAgo)
                    )
                    onSave(base.copy(amount = rial, isIncome = income, category = cat, note = note))
                }
            }) { Txt("ذخیره", 15, FontWeight.ExtraBold, Pal.violet) }
        },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = onDelete) { Txt("حذف", 14, color = Pal.coral) }
                TextButton(onClick = onDismiss) { Txt("انصراف", 14, color = Pal.soft) }
            }
        }
    )
}

// ───────────────────────── گزارش‌ها ─────────────────────────
@Composable
fun ReportsScreen(vm: MainVM) {
    val txs by vm.txs.collectAsState()
    val toman by vm.toman.collectAsState()
    var mode by remember { mutableIntStateOf(0) }

    val now = System.currentTimeMillis()
    val periods = if (mode == 0) Stats.weekPeriods(now, 8) else Stats.monthPeriods(now, 6)
    val sums = periods.map { Stats.sum(txs, it.from, it.to) }
    val cur = sums.last()
    val prev = sums[sums.size - 2]
    val curP = periods.last()
    val per = if (mode == 0) "هفته" else "ماه"
    val cats = Stats.byCategory(txs, curP.from, curP.to)

    fun cmp(a: Long, b: Long, what: String): String {
        if (b <= 0L) return "برای مقایسه ${what} به داده‌ی ${per} قبل نیاز داریم."
        val p = ((a - b) * 100.0 / b).roundToInt()
        return when {
            p > 0 -> what + " این " + per + " " + Fmt.fa(p) + "٪ بیشتر از " + per + " قبل بوده"
            p < 0 -> what + " این " + per + " " + Fmt.fa(abs(p)) + "٪ کمتر از " + per + " قبل بوده"
            else -> what + " این " + per + " تقریباً برابر " + per + " قبل بوده"
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("📅 هفتگی", mode == 0) { mode = 0 }
            Chip("🗓️ ماهانه", mode == 1) { mode = 1 }
        }

        Glass(Modifier.fillMaxWidth()) {
            Txt(if (mode == 0) "درآمد و هزینه ۸ هفته اخیر" else "درآمد و هزینه ۶ ماه اخیر", 14, FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Txt("🟢 درآمد", 11, color = Pal.mint, weight = FontWeight.Bold)
                Txt("🔴 هزینه", 11, color = Pal.coral, weight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            BarChart(periods.map { it.label }, sums.map { it.income }, sums.map { it.expense })
        }

        Glass(Modifier.fillMaxWidth()) {
            Txt("✨ این " + per + " (" + curP.label + ")", 14, FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Txt("درآمد: " + Fmt.money(cur.income, toman), 13, FontWeight.Bold, Pal.mint)
            Txt("هزینه: " + Fmt.money(cur.expense, toman), 13, FontWeight.Bold, Pal.coral)
            Txt(
                (if (cur.net >= 0) "پس‌انداز: " else "کسری: ") + Fmt.money(abs(cur.net), toman),
                13, FontWeight.ExtraBold, if (cur.net >= 0) Pal.violet else Pal.coral
            )
            Spacer(Modifier.height(8.dp))
            Txt("• " + cmp(cur.expense, prev.expense, "هزینه"), 12, color = Pal.soft)
            Txt("• " + cmp(cur.income, prev.income, "درآمد"), 12, color = Pal.soft)
        }

        Glass(Modifier.fillMaxWidth()) {
            Txt("🍰 دسته‌های پرخرج این " + per, 14, FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            if (cats.isEmpty()) {
                Txt("هزینه‌ای ثبت نشده.", 12, color = Pal.soft)
            } else {
                val mx = cats.first().second.toFloat()
                for (c in cats.take(6)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Txt(Cats.emoji(c.first) + " " + c.first, 13, modifier = Modifier.weight(1f))
                        Txt(Fmt.money(c.second, toman), 12, FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Progress(c.second / mx, Brush.horizontalGradient(listOf(Cats.color(c.first), Pal.violet)))
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        Glass(Modifier.fillMaxWidth()) {
            Txt("📋 جدول دوره‌ها", 14, FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            for (i in periods.indices.reversed()) {
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Txt(periods[i].label, 12, FontWeight.Bold, modifier = Modifier.width(80.dp))
                    Column(Modifier.weight(1f)) {
                        Txt("＋ " + Fmt.money(sums[i].income, toman), 11, color = Pal.mint)
                        Txt("－ " + Fmt.money(sums[i].expense, toman), 11, color = Pal.coral)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ───────────────────────── تنظیمات ─────────────────────────
@Composable
fun SettingsScreen(vm: MainVM, hasPerm: Boolean, onAskPerm: () -> Unit) {
    val toman by vm.toman.collectAsState()
    val budget by vm.budget.collectAsState()
    val loose by vm.loose.collectAsState()
    var budgetText by remember(budget, toman) {
        mutableStateOf(if (budget > 0) Fmt.disp(budget, toman).toString() else "")
    }
    var testText by remember { mutableStateOf("") }
    var testOut by remember { mutableStateOf("") }
    var confirmClear by remember { mutableStateOf(false) }

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) vm.exportCsv(uri)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Glass(Modifier.fillMaxWidth()) {
            Txt("📩 پیامک‌ها", 15, FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Txt("پیامک‌های جدید خودکار ثبت می‌شن. برای وارد کردن پیامک‌های قدیمی دکمه زیر رو بزن.", 12, color = Pal.soft)
            Spacer(Modifier.height(10.dp))
            PillButton(if (hasPerm) "🔄 خواندن دوباره صندوق پیامک" else "🔓 اجازه خواندن پیامک") {
                if (hasPerm) vm.importSms(false) else onAskPerm()
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Txt("حالت آزاد", 13, FontWeight.Bold)
                    Txt("پیامک هر فرستنده‌ای که مبلغ و کلمه‌ی واریز/برداشت داشت ثبت بشه", 11, color = Pal.soft)
                }
                Switch(checked = loose, onCheckedChange = { vm.setLoose(it) })
            }
        }

        Glass(Modifier.fillMaxWidth()) {
            Txt("💴 واحد پول", 15, FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("تومان", toman) { vm.setToman(true) }
                Chip("ریال", !toman) { vm.setToman(false) }
            }
        }

        Glass(Modifier.fillMaxWidth()) {
            Txt("🎯 بودجه ماهانه (" + Fmt.unit(toman) + ")", 15, FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = budgetText,
                onValueChange = { budgetText = Fmt.digitsOnly(it) },
                placeholder = { Txt("مثلاً ۲۰۰۰۰۰۰۰", 13, color = Pal.soft) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            if (budgetText.isNotEmpty()) {
                Txt(Fmt.num(budgetText.toLongOrNull() ?: 0L) + " " + Fmt.unit(toman), 12, color = Pal.violet)
            }
            Spacer(Modifier.height(8.dp))
            PillButton("ذخیره بودجه") {
                val v = budgetText.toLongOrNull() ?: 0L
                vm.setBudget(if (toman) v * 10 else v)
            }
        }

        Glass(Modifier.fillMaxWidth()) {
            Txt("🧪 آزمایش تحلیل پیامک", 15, FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Txt("متن یک پیامک بانک ملت رو اینجا بچسبون تا ببینی برنامه چطور می‌فهمدش.", 12, color = Pal.soft)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = testText,
                onValueChange = { testText = it },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            PillButton("تحلیل کن") {
                val r = SmsParser.parse("BankMellat", testText, loose)
                testOut = if (r == null) "❌ به‌عنوان تراکنش بانکی شناسایی نشد"
                else (if (r.income) "✅ درآمد" else "✅ هزینه") + " | مبلغ: " + Fmt.money(r.amount, toman) +
                        " | دسته: " + r.category +
                        (if (r.balance != null) " | مانده: " + Fmt.money(r.balance, toman) else "")
            }
            if (testOut.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Txt(testOut, 12, FontWeight.Bold, Pal.violet)
            }
        }

        Glass(Modifier.fillMaxWidth()) {
            Txt("🗂️ داده‌ها", 15, FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PillButton("📤 خروجی CSV") { csvLauncher.launch("hesabyar.csv") }
                PillButton("🗑️ پاک کردن همه", brush = Pal.warn) { confirmClear = true }
            }
            Spacer(Modifier.height(8.dp))
            Txt("همه‌ی اطلاعات فقط روی همین گوشی ذخیره می‌شه و به هیچ‌جا ارسال نمی‌شه.", 11, color = Pal.soft)
        }
        Spacer(Modifier.height(8.dp))
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            containerColor = Color.White,
            title = { Txt("پاک کردن همه تراکنش‌ها؟", 17, FontWeight.ExtraBold) },
            text = { Txt("این کار قابل برگشت نیست.", 13) },
            confirmButton = {
                TextButton(onClick = { vm.clearAll(); confirmClear = false }) { Txt("بله، پاک کن", 14, FontWeight.Bold, Pal.coral) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Txt("انصراف", 14, color = Pal.soft) }
            }
        )
    }
}
