package com.example.liquidglassdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiquidGlassDemoScreen()
        }
    }
}

@Composable
fun LiquidGlassDemoScreen() {
    // 背景底色会被一并画进 backdrop，避免玻璃采样到透明像素
    val backdrop = rememberLayerBackdrop {
        drawRect(Color(0xFFEFD9FF))
        drawContent()
    }

    Box(Modifier.fillMaxSize()) {
        Wallpaper(
            Modifier
                .layerBackdrop(backdrop)
                .fillMaxSize()
        )

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(48.dp))
            Text(
                "液态玻璃",
                style = TextStyle(color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Medium)
            )
            Text(
                "iOS 26 Liquid Glass · Android 复刻\nAGSL 折射 + 色散 + 深度模糊",
                style = TextStyle(color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, lineHeight = 20.sp)
            )
            Spacer(Modifier.height(8.dp))
            repeat(4) { index -> DemoCard(index) }
            Spacer(Modifier.height(220.dp))
        }

        DraggableGlassLens(backdrop)
        GlassBottomBar(backdrop)
    }
}

/** 纯 Canvas 绘制的彩色渐变壁纸，不依赖任何图片资源 */
@Composable
fun Wallpaper(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawRect(
            Brush.verticalGradient(
                listOf(Color(0xFFFFD9E8), Color(0xFFC7D8FF), Color(0xFFB9F5D0))
            )
        )
        val blobs = listOf(
            Triple(Color(0xFFFF5F6D), Offset(size.width * 0.12f, size.height * 0.16f), size.width * 0.55f),
            Triple(Color(0xFF6A82FB), Offset(size.width * 0.95f, size.height * 0.36f), size.width * 0.52f),
            Triple(Color(0xFFFFC371), Offset(size.width * 0.22f, size.height * 0.60f), size.width * 0.46f),
            Triple(Color(0xFF38B6FF), Offset(size.width * 0.88f, size.height * 0.86f), size.width * 0.50f)
        )
        blobs.forEach { (color, center, radius) ->
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.92f), color.copy(alpha = 0f)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }
    }
}

@Composable
fun DemoCard(index: Int) {
    val gradients = listOf(
        listOf(Color(0xFFFF5F6D), Color(0xFFFFC371)),
        listOf(Color(0xFF6A82FB), Color(0xFF38B6FF)),
        listOf(Color(0xFF059669), Color(0xFF34D399)),
        listOf(Color(0xFF7C3AED), Color(0xFFEC4899))
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(gradients[index % gradients.size])),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            "卡片 ${index + 1} · 拖动圆形玻璃试试折射",
            style = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(start = 20.dp)
        )
    }
}

/**
 * 可拖动的圆形"玻璃透镜"：模拟 iOS 26 的 Clear 风格玻璃，
 * 无磨砂、边缘强折射 + 色散 + 深度效果。
 */
@Composable
fun DraggableGlassLens(backdrop: Backdrop) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 170.dp)
                .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                .size(112.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { CircleShape },
                    effects = {
                        blur(2f.dp.toPx())
                        lens(
                            refractionHeight = 20f.dp.toPx(),
                            refractionAmount = 40f.dp.toPx(),
                            depthEffect = true,
                            chromaticAberration = true
                        )
                    },
                    onDrawSurface = {
                        // 轻微白面 + 顶部高光，模拟玻璃表面反光
                        drawRect(Color.White.copy(alpha = 0.10f))
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.30f), Color.Transparent),
                                center = Offset(size.width / 2f, size.height * 0.16f),
                                radius = size.width * 0.75f
                            )
                        )
                    }
                )
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        offset += dragAmount
                        change.consume()
                    }
                }
        )
    }
}

/**
 * 液态玻璃底部导航：vibrancy 提饱和 → blur 磨砂 → lens 边缘折射。
 * 对应 iOS 26 Regular 风格玻璃。
 */
@Composable
fun GlassBottomBar(backdrop: Backdrop) {
    var selected by remember { mutableIntStateOf(0) }
    val items = listOf(Icons.Filled.Home to "首页", Icons.Filled.Search to "搜索", Icons.Filled.Person to "我的")

    Row(
        Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
            .fillMaxWidth()
            .height(64.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(32f.dp) },
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    lens(
                        refractionHeight = 16f.dp.toPx(),
                        refractionAmount = 32f.dp.toPx(),
                        depthEffect = true,
                        chromaticAberration = true
                    )
                },
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.22f)) }
            ),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, (icon, label) ->
            val active = selected == index
            val contentColor = if (active) Color.White else Color.White.copy(alpha = 0.6f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(20f.dp))
                    .clickable { selected = index }
                    .padding(horizontal = 18f.dp, vertical = 8f.dp)
            ) {
                Icon(icon, contentDescription = label, tint = contentColor)
                Text(label, style = TextStyle(color = contentColor, fontSize = 10.sp))
            }
        }
    }
}

