# Sources · 全部来源

## 被提炼对象
- NPatch(免 root Xposed 框架,液态玻璃管理器)— https://github.com/7723mod/NPatch
  - 官网:https://npatch.nkbe.top
  - release v1.0.5 "The Buzzy Bees":新增 floating glass bottom bar / liquid glass effects
  - 上游谱系:LSPatch → Xpatch;核心 Vector(https://github.com/Nk-Be/Vector);UI 库 miuix-kmp(https://github.com/miuix-kotlin/miuix)
- liquid-glass-web(Apple Liquid Glass 的 Web 复刻,动效扩展)— https://github.com/xiaofangstudio/liquid-glass-web
  - 折射引擎移植自 shuding/liquid-glass — https://github.com/shuding/liquid-glass
  - 动效标准参考 Apple WWDC25《Meet Liquid Glass》
- Kyant0/AndroidLiquidGlass(backdrop 库,教学级配方所依赖)— https://github.com/Kyant0/AndroidLiquidGlass
  - 文档:https://kyant.gitbook.io/backdrop · Maven:`io.github.kyant0:backdrop:2.0.1`

## Android 生态其他方案(详见调研报告)
- QmDeve/AndroidLiquidGlassView(View 体系)— https://github.com/QmDeve/AndroidLiquidGlassView
- saratlse/LiquidGlassAndroid(Compose 实验复刻)— https://github.com/saratlse/LiquidGlassAndroid
- chrisbanes/haze(磨砂事实标准,Liquid Glass 双输入研究中 issue #1009)— https://github.com/chrisbanes/haze
- Kashif-E/KMPLiquidGlass(KMP)— https://github.com/Kashif-E/KMPLiquidGlass
- FloatingTabBar(悬浮玻璃底栏开源)— https://www.reddit.com/r/androiddev/comments/1lznuv6/

## 官方资料
- AGSL(RuntimeShader)— https://developer.android.com/develop/ui/views/graphics/agsl/using-agsl
- Window blurs — https://source.android.com/docs/core/display/window-blurs
- Compose Multiplatform 桥接 SwiftUI Liquid Glass — https://kotlinlang.org/docs/multiplatform/ios-liquid-glass.html
- Apple《Adopting Liquid Glass》(WWDC25)
- Web `feDisplacementMap` / `backdrop-filter` — MDN

## 本仓库产物与来源关系
| 路径 | 性质 | 来源/许可 |
|---|---|---|
| `android/compose-miuix/**` | 上游源码收录 | NPatch GPL-3.0(WeKit / compose-miuix-ui 谱系) |
| `android/compose-backdrop/GlassBottomBar.kt` | 原创(基于库 API) | 本仓库 MIT;库 Apache-2.0 |
| `web/bottombar-liquid-glass.html` | 原创(技术路线来自 shuding/liquid-glass) | 本仓库 MIT;技术 MIT |
| `docs/**`、`README.md`、`reference/npatch-apk-逆向分析.md` | 原创 | 本仓库 MIT |
| `reference/调研报告-*.md` | 原创(前次调研) | 本仓库 MIT |
