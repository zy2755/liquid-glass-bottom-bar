/**
 * GlassBottomBar —— 液态玻璃底部导航(教学级最小配方)
 *
 * 依赖:io.github.kyant0:backdrop:2.0.1(Apache-2.0)
 *       https://github.com/Kyant0/AndroidLiquidGlass
 * 要求:API 33+ 才有折射(lens/AGSL);API 31/32 自动降级为磨砂。
 *
 * 接线(三行):
 *   val backdrop = rememberLayerBackdrop { drawRect(surfaceColor); drawContent() }
 *   Content(Modifier.fillMaxSize().layerBackdrop(backdrop))   // 内容全屏采样,别加 Scaffold bottom padding!
 *   GlassBottomBar(backdrop)                                   // 列表尾部自己发 contentPadding
 */
package com.example.liquidglassdemo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

/**
 * 液态玻璃底部导航:对应 iOS 26 Regular 玻璃。
 * 效果链(顺序敏感):vibrancy 提饱和 → blur 磨砂 → lens 边缘折射 → 表面 tint 保证可读性。
 */
@Composable
fun GlassBottomBar(
    backdrop: Backdrop,
    items: List<Pair<ImageVector, String>> = listOf(
        Icons.Filled.Home to "首页",
        Icons.Filled.Search to "搜索",
        Icons.Filled.Person to "我的",
    ),
    selectedIndex: Int = 0,
    onSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .navigationBarsPadding()
            // 悬浮胶囊:不贴边,底部留白(12~20dp)+ 导航栏 inset
            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
            .fillMaxWidth()
            .height(64.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(32f.dp) }, // pill 全圆角
                effects = {
                    vibrancy()                          // ① 提饱和(saturation 1.5)
                    blur(4f.dp.toPx())                  // ② 磨砂
                    lens(                               // ③ 边缘折射(API 33+)
                        refractionHeight = 16f.dp.toPx(),
                        refractionAmount = 32f.dp.toPx(),
                        depthEffect = true,
                        chromaticAberration = true,     // ④ 色散
                    )
                },
                // ⑤ 表面:半透明白面保证 tab 文字对比度(暗色主题换 black 0.15)
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.22f)) },
            ),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, (icon, label) ->
            val active = selectedIndex == index
            val contentColor = if (active) Color.White else Color.White.copy(alpha = 0.6f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(20f.dp))
                    .clickable { onSelected(index) }
                    .padding(horizontal = 18f.dp, vertical = 8f.dp),
            ) {
                Icon(icon, contentDescription = label, tint = contentColor)
                Text(label, style = TextStyle(color = contentColor, fontSize = 10.sp))
            }
        }
    }
}
