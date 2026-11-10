# NOTICE · 第三方组件与许可

本仓库混合了原创内容与第三方代码,各部分许可如下:

## android/compose-miuix/(GPL-3.0)
来源:[7723mod/NPatch](https://github.com/7723mod/NPatch) v1.0.7
`manager/src/main/java/top/nkbe/npatch/ui/`:
- `FloatingGlassBottomBar.kt` — 文件头注明 "Ported from WeKit ui/content/FloatingBottomBar.kt;
  Adapted from compose-miuix-ui example (IosLiquidGlassNavigationBar)",GPL-3.0-only,
  Copyright (C) 2025-2026 InstallerX Revived contributors / NPatch contributors;
- `liquid/{Lens,InnerShadow,Vibrancy,CombinedBackdrop}.kt` — 同仓库 GPL-3.0;
  其中 Lens 的圆角矩形 SDF 折射 shader 与 [Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass)
  (Apache-2.0)同源。

**使用注意**:若在你的项目中复用本目录代码,衍生作品需遵循 GPL-3.0 开源。

## android/compose-backdrop/(MIT)
原创配方,调用 [Kyant0/AndroidLiquidGlass backdrop 库](https://github.com/Kyant0/AndroidLiquidGlass)
(库本身 Apache-2.0,Maven `io.github.kyant0:backdrop:2.0.1`)。

## web/bottombar-liquid-glass.html(MIT)
原创文件;折射算法思想来自:
- [shuding/liquid-glass](https://github.com/shuding/liquid-glass)(MIT)
- [xiaofangstudio/liquid-glass-web](https://github.com/xiaofangstudio/liquid-glass-web)(MIT)

## reference/调研报告-LiquidGlass-Android实现方案.md(MIT)
前次调研产物,引用了各开源仓库与官方文档,见 reference/sources.md。

## 商标与设计
"Liquid Glass" 为 Apple Inc. 的设计语言名称,本仓库仅为技术复刻研究,与 Apple 无关。
