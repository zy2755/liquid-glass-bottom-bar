# 02 · Android Compose:两条实现路线

## 路线 A:Kyant0 backdrop 库(教学级,推荐起步)

依赖(Maven Central):

```kotlin
implementation("io.github.kyant0:backdrop:2.0.1")
```

核心 API 只有三个:

| API | 作用 |
|---|---|
| `rememberLayerBackdrop { drawRect(bg); drawContent() }` | 创建采样层(先填底色!) |
| `Modifier.layerBackdrop(backdrop)` | 把某块内容登记进采样 |
| `Modifier.drawBackdrop(backdrop, shape, effects, onDrawSurface)` | 对采样应用效果链并绘制玻璃 |

效果 DSL(顺序敏感):`vibrancy() → blur(r) → lens(refractionHeight, refractionAmount, depthEffect, chromaticAberration)`。

完整可运行底栏配方见 [`android/compose-backdrop/GlassBottomBar.kt`](../android/compose-backdrop/GlassBottomBar.kt),
要点摘录:

```kotlin
Row(
    Modifier
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)   // 悬浮:不贴边
        .fillMaxWidth()
        .height(64.dp)
        .drawBackdrop(
            backdrop = backdrop,
            shape = { RoundedCornerShape(32f.dp) },            // pill
            effects = {
                vibrancy()
                blur(4f.dp.toPx())
                lens(refractionHeight = 16f.dp.toPx(), refractionAmount = 32f.dp.toPx(),
                     depthEffect = true, chromaticAberration = true)
            },
            onDrawSurface = { drawRect(Color.White.copy(alpha = 0.22f)) },  // 可读性 tint
        )
)
```

经验值:`lens(refractionHeight≈高度的1/4, refractionAmount≈height的1/2)` 起步再微调;
`blur 4dp` 底栏够用(太大 → 文字后面一坨糊),可拖动大透镜才用 `2dp` 低模糊 + 强折射(Clear 玻璃风格)。

**限制**:`lens` 依赖 AGSL `RuntimeShader`,需 **API 33+**;API 31/32 只有 `blur`(库自动降级为磨砂)。

## 路线 B:NPatch 生产级(miuix-kmp blur 模块)

NPatch 管理器(`top.nkbe.npatch`)的底栏,生产验证过的增强点(相对路线 A):

1. **`FloatingBottomBarMode` 三模式**:`LiquidGlass / Blur / None`,运行时按能力和开关切换:

```kotlin
// LiquidGlass 模式(面板)
effects = {
    if (!isGlassTransparent) {
        vibrancy(); blur(liquidGlassBlurRadius.toPx())
        lens(refractionHeight = 24.dp.toPx(), refractionAmount = 24.dp.toPx())
        // ⚠️ 24dp 是 NPatch 的选择(边缘弯曲带厚,中心平坦区仅剩 16dp,扭曲感强);
        //    带文字标签的底栏建议 12~16dp 起步,更接近 iOS 26 "大平中心+薄边缘圈"(见 docs/04 表头说明)
    }
},
highlight = { baseHighlight.copy(alpha = 0.75f) },   // 镜面高光(BloomStroke 双光源)
layerBlock = { /* pressProgress → 面板微膨胀 lerp(1f, 1f+16dp/width, p) */ },
onDrawSurface = { drawRect(containerColor) },        // containerColor = 0.4 alpha

// Blur 降级模式(API 31+ 但无折射 / 用户关闭折射)
effects = { blur(25.dp.toPx(), 25.dp.toPx()) },
onDrawSurface = { drawRect(containerColor.copy(alpha = 0.65f)) },
```

2. **`liquid/` 四件套**(本仓库已收录,可直接复用):
   - [`Lens.kt`](../android/compose-miuix/liquid/Lens.kt):圆角矩形 SDF 折射 AGSL shader。
     亮点:**七次采样的光谱色散** —— 把色散拆成 red→purple 七段、按 1/3.5 与 1/7 加权平均,
     色散强度 `chromaticAberration * (cx·cy)/(hx·hy)`(越靠角落越强);
     还有 `offset` uniform 支持采样随容器移动而平移。
   - [`InnerShadow.kt`](../android/compose-miuix/liquid/InnerShadow.kt):内阴影 Modifier,
     offscreen GraphicsLayer 画 outline + `BlendMode.Clear` 挖洞,支持模糊半径/偏移/alpha,按压下陷就用它。
   - [`Vibrancy.kt`](../android/compose-miuix/liquid/Vibrancy.kt):`colorControls(saturation = 1.5f)`。
   - [`CombinedBackdrop.kt`](../android/compose-miuix/liquid/CombinedBackdrop.kt):两个 backdrop 相加。

3. **指示器跟手**(iOS 26 tab bar 拖拽切换观感),核心是 `DampedDragAnimation`:
   - 手指横滑时 `targetValue` 连续变化 → 指示胶囊实时跟随;
   - 面板橡皮筋:`4dp · sign · EaseOut(|fraction|)`;
   - 松手 `fastRoundToInt` 落到最近 tab,spring 回弹 `spring(1f, 300f, 0.5f)`;
   - 速度挤压拉伸(玻璃"液体感"):
     ```kotlin
     val velocity = dampedDragAnimation.velocity / 10f
     scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
     scaleY  *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
     ```

4. **重力感应高光**:`rememberDeviceTilt()` 读加速度计,把高光位置向量绕 z 轴旋转
   (面板 `-45°`、指示器 `+90°`),设备倾斜时镜面反光跟着"流"。
   无传感器/不支持时优雅回退为静态光。

5. **接线模板**([MainScreen.kt] 的做法):

```kotlin
val backdrop = if (enableGlass) rememberLayerBackdrop { drawRect(surfaceColor); drawContent() } else null
Content(Modifier.fillMaxSize().layerBackdrop(backdrop!!))     // 全屏采样,不要 Scaffold padding!
LazyColumn(contentPadding = PaddingValues(bottom = barHeight + 20.dp + navInset)) // 自己发尾部留白
FloatingGlassBottomBar(..., backdrop = backdrop,
    mode = if (enableGlass) LiquidGlass else None)
```

## 选型建议

| 需求 | 选 |
|---|---|
| 快速验证 / 学习原理 | 路线 A(30 行,依赖少) |
| 生产应用,要完整交互与降级 | 路线 B(注意 GPL-3.0 合规)或参照其思路在路线 A 上补齐 |
| 只要磨砂不要折射(API 31/32) | 两路线的 Blur 模式,或 chrisbanes/haze |
| View 体系老项目 | QmDeve/AndroidLiquidGlassView(见 reference/调研报告) |
