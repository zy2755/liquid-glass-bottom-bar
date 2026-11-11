# sample-app · Liquid Glass 底栏可运行工程（Android · Jetpack Compose）

本仓库的**可一键运行示例**:在 Android 上复刻 Apple iOS 26「液态玻璃（Liquid Glass）」底栏
与可拖动玻璃透镜。基于开源库 [Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass)
（Maven Central 坐标 `io.github.kyant0:backdrop:2.0.1`，Apache 2.0）。
`app/src/main/java/com/example/liquidglassdemo/MainActivity.kt` 内的 `GlassBottomBar`
即 [../compose-backdrop/GlassBottomBar.kt](../compose-backdrop/GlassBottomBar.kt) 配方的原始出处。

## Demo 内容

| 组件 | 对应 iOS 26 风格 | 实现要点 |
|---|---|---|
| `DraggableGlassLens` 可拖动圆形透镜 | Clear 玻璃 | 低模糊 + 强边缘折射（`lens`）+ 色散（`chromaticAberration`）+ 深度效果（`depthEffect`） |
| `GlassBottomBar` 底部导航 | Regular 玻璃 | `vibrancy()` 提饱和 → `blur()` 磨砂 → `lens()` 边缘折射 → `onDrawSurface` 半透明白面保证可读性 |

壁纸为纯 Canvas 绘制的渐变色块，无需图片资源；玻璃组件实时对背后内容做折射采样，滚动/拖动时效果连续。

## 构建与运行

1. 用 **Android Studio**（Ladybug 或更新）打开本目录，等待 Gradle Sync（会自动下载 Gradle 8.9 与依赖）。
   - 如提示缺少 `gradle-wrapper.jar`，让 Android Studio 自动修复，或在终端执行 `gradle wrapper`。
2. 连接 **Android 13 (API 33) 及以上** 的真机或模拟器，点 Run。
3. 最低 API 31（Android 12L）：可编译运行，但 `lens` 折射依赖 AGSL `RuntimeShader`，
   在 API 31/32 上库会自动降级（仅磨砂模糊，无折射）。想要完整效果请用 API 33+ 设备。

- 工具链：JDK 17、AGP 8.7.3、Kotlin 2.1.21、Compose BOM 2024.12.01
- 若出现 Kotlin metadata 版本报错（库使用了更新的 Kotlin 编译），把 `build.gradle.kts` 里的 Kotlin 版本升到最新稳定版即可。

## 技术原理（五层合成）

iOS 26 的 Liquid Glass 本质是一块"实时采样背后内容的玻璃板"，Android 上按同样的分层模型复刻：

```
┌ 5. 表面处理：白面/着色 tint、内描边高光、按压高光（onDrawSurface / BlendMode.Hue）
├ 4. 边缘透镜折射：按圆角矩形 SDF(有向距离场) 构建高度场，
│    用 AGSL RuntimeShader 沿法线位移采样背景像素 → 边缘出现"液态"折射
│    （可选 RGB 三通道分别位移 → 色散 chromatic aberration）
├ 3. 磨砂模糊：RenderEffect.createBlurEffect（API 31+），配合饱和度提升 vibrancy
├ 2. 背景采样：库用 graphicsLayer/RenderNode 把背后内容离屏复制一份（LayerBackdrop）
└ 1. 内容与壁纸
```

对应 API 层级：

- **API 33+（Android 13）**：AGSL `RuntimeShader` —— 真正的折射/色散/深度模糊（本 demo 的 lens 效果）
- **API 31+（Android 12）**：`RenderEffect` 模糊 —— 磨砂玻璃
- 更低版本：只能用"半透明 + 渐变高光"近似，没有真折射

## 相关资源（调研结论）

- Kyant0/AndroidLiquidGlass —— 本工程所用，Compose Multiplatform，文档 https://kyant.gitbook.io/backdrop
- QmDeve/AndroidLiquidGlassView —— View 体系实现（真实折射 + 色散）
- saratlse/LiquidGlassAndroid —— Compose 实验性复刻（折射/色散/弹性按压）
- chrisbanes/haze —— Compose 背景模糊库，正在跟进 Liquid Glass 式双输入渲染（issue #1009）
- Flutter：`liquid_glass_renderer`、`liquid_glass_widgets`
- React Native：`expo-glass-effect` / `callstack/liquid-glass`（均为 iOS 专属，Android 需自绘或用 expo-blur 近似）
- 官方资料：Android AGSL 文档、Window blurs 文档、Apple《Adopting Liquid Glass》

完整调研见 [../../docs/](../../docs/) 与 [../../reference/](../../reference/)。
