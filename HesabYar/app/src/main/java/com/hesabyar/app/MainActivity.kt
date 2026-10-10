package com.hesabyar.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Repo.init(this)
        setContent { AppRoot() }
    }
}

@Composable
fun AppRoot() {
    val vm: MainVM = viewModel()
    val ctx = LocalContext.current

    fun check(): Boolean =
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED

    var hasPerm by remember { mutableStateOf(check()) }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasPerm = check()
        if (hasPerm) vm.importSms(false)
        else toast(ctx, "اجازه داده نشد. اگه اندروید ۱۳ به بالا داری، «راهنمای نصب» رو ببین (محدودیت برنامه‌های خارج از فروشگاه).")
    }
    val askPerm: () -> Unit = {
        permLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS))
    }

    LaunchedEffect(hasPerm) { if (hasPerm) vm.importSms(true) }

    var tab by rememberSaveable { mutableIntStateOf(0) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = lightColorScheme(primary = Pal.violet)) {
            Box(Modifier.fillMaxSize().background(Pal.bg)) {
                Column(Modifier.fillMaxSize().systemBarsPadding()) {
                    Box(Modifier.weight(1f)) {
                        when (tab) {
                            0 -> HomeScreen(vm, hasPerm, askPerm)
                            1 -> TxScreen(vm)
                            2 -> ReportsScreen(vm)
                            else -> SettingsScreen(vm, hasPerm, askPerm)
                        }
                    }
                    BottomBar(tab) { tab = it }
                }
            }
        }
    }
}

@Composable
fun BottomBar(tab: Int, onTab: (Int) -> Unit) {
    val items = listOf("🏠" to "خانه", "🧾" to "تراکنش‌ها", "📊" to "گزارش", "⚙️" to "تنظیمات")
    Row(
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .shadow(8.dp, RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .padding(6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEachIndexed { i, item ->
            val sel = i == tab
            val bg = if (sel) Modifier.background(Pal.grad) else Modifier
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .then(bg)
                    .clickable { onTab(i) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Txt(item.first, 20)
                Txt(item.second, 11, FontWeight.Bold, if (sel) Color.White else Pal.soft)
            }
        }
    }
}
