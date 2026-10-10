package com.hesabyar.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Pal {
    val ink = Color(0xFF3B2F5C)
    val soft = Color(0xFF8A7FA8)
    val violet = Color(0xFF9B6BFF)
    val pink = Color(0xFFFF7EB6)
    val mint = Color(0xFF2FD3A0)
    val coral = Color(0xFFFF6B8B)
    val sky = Color(0xFF6CC6FF)
    val sun = Color(0xFFFFD66B)
    val bg = Brush.verticalGradient(listOf(Color(0xFFFFE4F1), Color(0xFFEBE4FF), Color(0xFFDDF3FF)))
    val grad = Brush.horizontalGradient(listOf(Color(0xFFFF7EB6), Color(0xFF9B6BFF)))
    val warn = Brush.horizontalGradient(listOf(Color(0xFFFF6B8B), Color(0xFFFFB86B)))
}

@Composable
fun Txt(
    text: String,
    size: Int = 14,
    weight: FontWeight = FontWeight.Normal,
    color: Color = Pal.ink,
    modifier: Modifier = Modifier,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size.sp,
        fontWeight = weight,
        textAlign = align,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun Glass(
    modifier: Modifier = Modifier,
    pad: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    var m = modifier.shadow(4.dp, shape).background(Color.White.copy(alpha = 0.88f), shape)
    if (onClick != null) m = m.clip(shape).clickable(onClick = onClick)
    Column(m.padding(pad), content = content)
}

@Composable
fun PillButton(text: String, modifier: Modifier = Modifier, brush: Brush = Pal.grad, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(brush)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Txt(text, 14, FontWeight.Bold, Color.White)
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val bg = if (selected) Modifier.background(Pal.grad) else Modifier.background(Color(0x14000000))
    Box(Modifier.clip(shape).then(bg).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp)) {
        Txt(text, 13, FontWeight.Bold, if (selected) Color.White else Pal.ink)
    }
}

@Composable
fun Progress(frac: Float, brush: Brush, modifier: Modifier = Modifier) {
    val f by animateFloatAsState(frac.coerceIn(0f, 1f), tween(800), label = "progress")
    Box(
        modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0x1A000000))
    ) {
        Box(
            Modifier
                .fillMaxWidth(f.coerceAtLeast(0.02f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(brush)
        )
    }
}

@Composable
fun Donut(slices: List<Pair<Color, Float>>, modifier: Modifier = Modifier, center: @Composable () -> Unit) {
    val prog = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        prog.snapTo(0f)
        prog.animateTo(1f, tween(900))
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.17f
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val total = slices.sumOf { it.second.toDouble() }.toFloat()
            if (total <= 0f) {
                drawArc(
                    color = Color(0x1A000000), startAngle = 0f, sweepAngle = 360f, useCenter = false,
                    topLeft = Offset(inset, inset), size = arcSize, style = Stroke(stroke)
                )
            } else {
                var start = -90f
                for (s in slices) {
                    val sweep = s.second / total * 360f * prog.value
                    drawArc(
                        color = s.first, startAngle = start, sweepAngle = (sweep - 2f).coerceAtLeast(0.5f),
                        useCenter = false, topLeft = Offset(inset, inset), size = arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                    start += sweep
                }
            }
        }
        center()
    }
}

@Composable
private fun Bar(frac: Float, color: Color) {
    val h by animateFloatAsState(frac, tween(700), label = "bar")
    Box(
        Modifier
            .width(12.dp)
            .fillMaxHeight(h.coerceIn(0.015f, 1f))
            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .background(color)
    )
}

@Composable
fun BarChart(labels: List<String>, income: List<Long>, expense: List<Long>, modifier: Modifier = Modifier) {
    val maxV = maxOf(1L, (income + expense).maxOrNull() ?: 1L).toFloat()
    Row(
        modifier.fillMaxWidth().height(190.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in labels.indices) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    Modifier.height(150.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Bar(income[i] / maxV, Pal.mint)
                    Bar(expense[i] / maxV, Pal.coral)
                }
                Txt(labels[i], 10, color = Pal.soft, align = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

/** mood: 0 = شاد، 1 = معمولی، 2 = نگران */
@Composable
fun Mascot(mood: Int, modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "mascot")
    val bob by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val tick by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3600, easing = LinearEasing)),
        label = "blink"
    )
    val blink = if (tick > 0.95f) 0.12f else 1f

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f - bob * h * 0.03f
        val hair = Color(0xFFFF8CC6)
        val skin = Color(0xFFFFE3D2)
        val dark = Color(0xFF2A1B5C)

        // دم‌اسبی‌ها و موی پشت
        drawOval(hair, Offset(cx - w * 0.50f, cy - h * 0.02f), Size(w * 0.24f, h * 0.50f))
        drawOval(hair, Offset(cx + w * 0.26f, cy - h * 0.02f), Size(w * 0.24f, h * 0.50f))
        drawCircle(hair, w * 0.40f, Offset(cx, cy - h * 0.04f))
        // صورت
        drawOval(skin, Offset(cx - w * 0.32f, cy - h * 0.22f), Size(w * 0.64f, h * 0.60f))
        // کلاه موی جلو (چتری)
        val bangs = Path().apply {
            moveTo(cx - w * 0.34f, cy - h * 0.02f)
            quadraticBezierTo(cx - w * 0.36f, cy - h * 0.36f, cx, cy - h * 0.36f)
            quadraticBezierTo(cx + w * 0.36f, cy - h * 0.36f, cx + w * 0.34f, cy - h * 0.02f)
            lineTo(cx + w * 0.22f, cy - h * 0.14f)
            lineTo(cx + w * 0.12f, cy - h * 0.04f)
            lineTo(cx, cy - h * 0.15f)
            lineTo(cx - w * 0.12f, cy - h * 0.04f)
            lineTo(cx - w * 0.22f, cy - h * 0.14f)
            close()
        }
        drawPath(bangs, hair)
        // گل سر
        drawCircle(Color(0xFFFFD66B), w * 0.05f, Offset(cx + w * 0.22f, cy - h * 0.25f))
        drawCircle(Color.White, w * 0.02f, Offset(cx + w * 0.22f, cy - h * 0.25f))

        // چشم‌ها و ابرو
        for (sign in listOf(-1f, 1f)) {
            val ex = cx + sign * w * 0.17f
            val ey = cy + h * 0.07f
            val eh = h * 0.20f * blink
            drawOval(Color(0xFF5B3FA8), Offset(ex - w * 0.085f, ey - eh / 2f), Size(w * 0.17f, eh))
            drawOval(dark, Offset(ex - w * 0.05f, ey - eh * 0.30f), Size(w * 0.10f, eh * 0.60f))
            if (blink > 0.5f) {
                drawCircle(Color.White, w * 0.03f, Offset(ex - w * 0.022f, ey - eh * 0.20f))
                drawCircle(Color.White, w * 0.013f, Offset(ex + w * 0.022f, ey + eh * 0.15f))
            }
            val outer = Offset(ex + sign * w * 0.09f, if (mood == 2) ey - h * 0.15f else ey - h * 0.18f)
            val inner = Offset(ex - sign * w * 0.08f, if (mood == 2) ey - h * 0.23f else ey - h * 0.18f)
            drawLine(dark, outer, inner, strokeWidth = w * 0.014f, cap = StrokeCap.Round)
            // گونه
            drawOval(Color(0x66FF6B8B), Offset(ex - w * 0.07f, ey + h * 0.11f), Size(w * 0.14f, h * 0.07f))
        }

        // دهان
        val my = cy + h * 0.23f
        when (mood) {
            0 -> drawArc(
                Color(0xFFD94679), 0f, 180f, true,
                Offset(cx - w * 0.07f, my - h * 0.04f), Size(w * 0.14f, h * 0.10f)
            )
            1 -> drawLine(
                dark, Offset(cx - w * 0.04f, my), Offset(cx + w * 0.04f, my),
                strokeWidth = w * 0.014f, cap = StrokeCap.Round
            )
            else -> {
                drawArc(
                    dark, 200f, 140f, false,
                    Offset(cx - w * 0.06f, my), Size(w * 0.12f, h * 0.08f),
                    style = Stroke(w * 0.014f, cap = StrokeCap.Round)
                )
                drawCircle(Color(0xFF6CC6FF), w * 0.03f, Offset(cx + w * 0.30f, cy - h * 0.10f))
            }
        }
    }
}
