# 调研报告：iOS 26 液态玻璃（Liquid Glass）在 Android 应用上的实现方案

> 调研时间：2026-10-27 · 来源：GitHub、Maven Central、官方文档、Reddit、Hacker News、开发者博客

## 一、背景

Apple 在 iOS 26 引入 Liquid Glass 设计语言：界面控件变成一块能对背后内容实时
**折射（refraction）、色散（chromatic aberration）、模糊（blur）、提饱和（vibrancy）**
并带边缘镜面高光的"玻璃板"。Android 没有官方等价 API，社区方案均为自行复刻，核心技术路线一致：

```
背景采样(Backdrop copy) → 模糊+提饱和 → 边缘 SDF 高度场位移折射(AGSL) → 表面高光/着色
```

Android 的能力分级：

| API 级别 | 能力 |
|---|---|
| API 33+ (Android 13) | `RuntimeShader`/AGSL —— 可实现真折射、色散、深度模糊（效果最完整） |
| API 31+ (Android 12) | `RenderEffect` 模糊 —— 磨砂玻璃；系统级 Window Blur 也可用 |
| 更低版本 | 只能"半透明 + 渐变高光"近似，或对壁纸做双重绘制模拟 |

## 二、Android 原生（Kotlin / Jetpack Compose）方案 ★重点

### 1. Kyant0/AndroidLiquidGlass（本 demo 采用）
- 仓库：https://github.com/Kyant0/AndroidLiquidGlass （Compose Multiplatform，kmp 分支）
- 文档：https://kyant.gitbook.io/backdrop
- Maven Central：`io.github.kyant0:backdrop:2.0.1`（最新稳定，2026-08 发布）
- 特点：目前最成熟、性能最好的 Compose 实现。核心 API：
  - `rememberLayerBackdrop` / `Modifier.layerBackdrop(backdrop)` —— 把背景内容离屏复制
  - `Modifier.drawBackdrop(backdrop, shape, effects, onDrawSurface)` —— 对采样应用效果链
  - 效果 DSL（顺序敏感）：`colorFilter ⇒ blur ⇒ lens`
    `vibrancy()`、`blur(radius)`、`lens(refractionHeight, refractionAmount, depthEffect, chromaticAberration)`
  - lens 基于 AGSL RuntimeShader + 圆角矩形 SDF 高度场，要求 shape 为 CornerBasedShape
- 限制：lens 需要 Android 13+；模糊效果 Android 12+

### 2. QmDeve/AndroidLiquidGlassView
- 仓库：https://github.com/QmDeve/AndroidLiquidGlassView
- 面向 **View 体系**（非 Compose），宣称"真实折射 + 色散"，适合老项目

### 3. saratlse/LiquidGlassAndroid
- 仓库：https://github.com/saratlse/LiquidGlassAndroid
- Compose 实验性复刻：折射、色散、弹性按压动画，适合学习 AGSL 写法

### 4. chrisbanes/haze
- 仓库：https://github.com/chrisbanes/haze （Compose 背景模糊的事实标准库）
- issue #1009「Investigate Android dual-input rendering for Liquid Glass」：
  正在研究"原图 + 预模糊图"双输入 runtime shader 通道来实现液态玻璃深度效果
  https://github.com/chrisbanes/haze/issues/1009

### 5. Kashif-E/KMPLiquidGlass
- 仓库：https://github.com/Kashif-E/KMPLiquidGlass
- Kotlin Multiplatform：Android / iOS / Desktop / Web 统一 API

### 6. 其他社区动态
- r/androiddev《Product wants "parity" with iOS's new Liquid glass》——产品要求对齐 iOS 视觉时
  Android 开发者的实操讨论：https://www.reddit.com/r/androiddev/comments/1o1cn5g/
- FloatingTabBar（模仿 Liquid Glass 的开源悬浮底栏）发布帖：https://www.reddit.com/r/androiddev/comments/1lznuv6/
- AGSL 官方文档：https://developer.android.com/develop/ui/views/graphics/agsl/using-agsl
- Window Blurs 官方文档：https://source.android.com/docs/core/display/window-blurs
- 实时玻璃 shader 深度文章（AGSL + Compose）：
  https://abduzeedo.com/crafting-glass-real-time-glass-shader-android

## 三、跨平台框架方案

### Flutter
- `liquid_glass_renderer`（pub.dev）：GPU shader 渲染真实折射，含 `FakeGlass` 降级路径
  https://pub.dev/packages/liquid_glass_renderer
- `sdegenaar/liquid_glass_widgets`：shader 模糊 + 物理"果冻"动画的组件套件
  https://github.com/sdegenaar/liquid_glass_widgets
- `flutter_liquid_glass_widgets`：支持 iOS/Android/macOS/Web/Windows

### React Native / Expo
- `expo-glass-effect`：官方封装，但**仅 iOS**（UIVisualEffectView），无 Android 实现
  https://docs.expo.dev/versions/latest/sdk/glass-effect
- `callstack/liquid-glass`：同样仅 iOS 26+，旧平台自动回退普通视图
  https://github.com/callstack/liquid-glass
- Android 上的 RN 方案：社区自写 Expo Module（blur + shader），或 `expo-blur` 近似
  https://www.reddit.com/r/reactnative/comments/1l7p63w/

### Compose Multiplatform 官方指引
- 在 CMP 中直接桥接 SwiftUI 原生 Liquid Glass：
  https://kotlinlang.org/docs/multiplatform/ios-liquid-glass.html

## 四、Web 端（用于快速预览效果）
- 主流做法：`backdrop-filter: url(#svgFilter)` + `feImage(位移贴图) + feDisplacementMap + feGaussianBlur + feColorMatrix(saturate)`
- 纯 CSS 讨论帖：https://www.reddit.com/r/css/comments/1lradww/
- 本任务的 `liquid-glass-demo.html` 即用此技术实现，可交互调参

## 五、选型建议

| 场景 | 推荐 |
|---|---|
| Jetpack Compose 新项目 | **Kyant0/AndroidLiquidGlass（backdrop）**，API 设计最贴近 iOS 语义 |
| 老 View 项目 | QmDeve/AndroidLiquidGlassView |
| 只要磨砂不要折射 | chrisbanes/haze（兼容性最好） |
| 想自己造轮子 / 学习原理 | 读 AGSL 文档 + saratlse 源码，用 RuntimeShader + SDF 位移自绘 |
| Flutter / RN | 用上表对应库；RN 官方生态目前无 Android 真折射方案 |

设备策略：`Build.VERSION.SDK_INT >= 33` 走完整折射链，否则降级为磨砂模糊 ——
上述库大多已内置降级逻辑。
