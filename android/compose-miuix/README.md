# compose-miuix —— NPatch 生产级底栏源码(GPL-3.0)

来源:[7723mod/NPatch](https://github.com/7723mod/NPatch) `manager/src/main/java/top/nkbe/npatch/ui/`
(v1.0.7,文件头保留原始许可与移植声明)。许可见本目录 [LICENSE](LICENSE)(GPL-3.0)与根目录 [NOTICE.md](../../NOTICE.md)。

## 文件清单

```
FloatingGlassBottomBar.kt     ← 底栏本体(三层玻璃结构 + 阻尼拖拽指示器)
liquid/
  Lens.kt                     ← AGSL 圆角矩形 SDF 折射 + 七段光谱色散
  InnerShadow.kt              ← 内阴影 Modifier(按压下陷)
  Vibrancy.kt                 ← 提饱和(saturation 1.5)
  CombinedBackdrop.kt         ← 两个 backdrop 相加
miuix/animation/
  DampedDragAnimation.kt      ← 拖拽跟手 + 速度采样(指示器"液体感"核心)
  InteractiveHighlight.kt     ← 按压/悬停交互高光(RuntimeShader)
miuix/modifier/
  DragGestureInspector.kt     ← awaitEachGesture 手势检查(inspectDragGestures)
```

## 编译依赖(重要)

1. **`top.yukonga.miuix.kmp.blur.*`**(Backdrop/drawBackdrop/layerBackdrop/highlight/sensor 等)
   来自 [miuix](https://github.com/miuix-kotlin/miuix) 的 blur 模块 —— **目前未发布到 Maven**,
   需要把 miuix 对应模块源码引入工程,或替换为 [Kyant0 backdrop](https://github.com/Kyant0/AndroidLiquidGlass)
   的同名 API(`drawBackdrop/effects{}/lens` 语义几乎一致,可参考 `../sample-app` 的用法);
2. `org.intellij.lang.annotations.Language`(来自 `org.jetbrains:annotations`,Compose 工程通常传递自带;
   没有就 `implementation("org.jetbrains:annotations:26.0.2")`);
3. `lens`/`InteractiveHighlight` 的 RuntimeShader 需要 **API 33+**;`blur` 需要 API 31+(见 docs/05)。

> **想立刻跑起来?** 别从这个目录开始 —— 用 [`../sample-app`](../sample-app)
> (完整 Gradle 工程,Kyant0 backdrop 库,Maven 一行依赖,Android Studio 直接 Run)。
> 本目录是"生产级参考实现",适合在已有工程里移植。
