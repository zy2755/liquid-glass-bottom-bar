# NPatch APK 逆向分析记录(证据链)

> 对象:`npatch液态玻璃效果.apk`(6.7MB)
> 结论:NPatch 管理器 v1.0.7,包名 `top.nkbe.npatch`,LSPatch 分支的免 root Xposed 框架,
> 其"液态玻璃"底栏为 **Jetpack Compose + miuix-kmp blur(背景采样)+ AGSL 折射 shader**。
> 逆向结论已与上游开源仓库 [7723mod/NPatch](https://github.com/7723mod/NPatch) 源码交叉核对,一致。

## 一、APK 结构取证

```
assets/npatch/loader.bin        ← 注入目标 APK 的 loader(DEX,"dex.039" 魔数)
assets/npatch/metaloader.dex    ← metaloader(含 npatch.metaloader.LSPAppComponentFactoryStub)
assets/npatch/so/*/libnpatch.so ← 原生库
assets/npatch.key / fpa_app.key ← 签名/配置(FPA1/FunProject 证书)
classes.dex (4.6MB)             ← 管理器本体:Compose UI + Room + Shizuku + okhttp
```

- `classes.dex` 中 `npatch.metaloader.LSPAppComponentFactoryStub`、`npatch.database.entity.Scope/LoadedModule`
  → 确认为 LSPatch 谱系(LSPosed 核心、Apkzlib 重打包、Shizuku 辅助安装);
- 版本字符串 `1.0.7`,权限含 `REQUEST_INSTALL_PACKAGES`、`QUERY_ALL_PACKAGES`、Shizuku V3。

## 二、UI 技术栈取证(dex 字符串)

| 证据(dex 中字符串) | 结论 |
|---|---|
| `androidx.compose.*`(大量) | Jetpack Compose 应用 |
| `chrisbanes`、`haze`、haze 高斯模糊 shader(含 `chrisbanes/haze/issues/520` 注释) | 集成/参考过 haze 库 |
| `top.yukonga.miuix.kmp.blur.*`(`Backdrop/drawBackdrop/layerBackdrop/rememberLayerBackdrop/highlight/sensor`) | miuix-kmp 的背景采样 blur 模块(Kyant0 backdrop 谱系) |
| AGSL shader:`sdRoundedRect / circleMap / refractionHeight / refractionAmount / depthEffect` | 圆角矩形 SDF 折射 shader,与 Kyant0 lens 同源 |
| `uniform shader cornerSdf; cornerSdf.eval(uv * bitmapSize).a` | 另一路径:SDF 以位图纹理传入 |
| 字段名池:`useFloatingGlassBottomBar`、`useFloatingGlassBottomBarBlur`、`useMonet` | 设置项(底栏玻璃开关) |

## 三、与上游源码核对(GitHub: 7723mod/NPatch, GPL-3.0)

| 文件 | 逆向所见 ↔ 源码 |
|---|---|
| `ui/component/FloatingGlassBottomBar.kt`(749 行) | 三层玻璃结构、DampedDragAnimation、BloomStroke 重力高光、三模式 |
| `ui/component/liquid/{Lens,InnerShadow,Vibrancy,CombinedBackdrop}.kt` | dex 中 shader 与 `Lens.kt` 逐字一致 |
| `config/ThemeConfig.kt` | `USE_FLOATING_GLASS_BOTTOM_BAR(_BLUR)`、`isFloatingGlassBottomBarBlurSupported() = SDK>=33` |
| `ui/page/MainScreen.kt` | `rememberLayerBackdrop{drawRect;drawContent()}` + 内容全屏采样 + "Scaffold padding 会裁采样"注释 |
| release notes v1.0.5 "The Buzzy Bees" | "Added theme switching, **floating glass bottom bar**, and liquid glass effects" |

## 四、方法论(可复用的 APK 分析流程)

1. `unzip` 解包 → 看 `assets/` 有无 patcher 谱系目录(loader.bin 魔数 `dex\n` 判 DEX);
2. 二进制 AndroidManifest:自写 AXML 字符串池解析(注意 chunk 内偏移:pool 头 8 字节、
   START_ELEMENT 的 ns/name 在 +16、attrs 在 +24)拿包名/版本/组件;
3. dex 关键字 grep:`Compose|React|Flutter`(框架)、`haze|backdrop|Liquid|Glass`(UI)、
   `RuntimeShader|AGSL|refraction`(折射);
4. AGSL shader 以**多行字符串**存于 dex 字符串池,按 `[\x20-\x7e\n]{30,}` 提取后按
   `uniform/half4/sdRoundedRect` 归组;
5. R8 混淆下找**字段名池**(`, useXxx=` 形态)定位设置类;再以上游开源仓库交叉核对;
6. 敏感注意:免 root 框架属双刃工具,本文仅作 UI 实现研究,不含绕过/破解内容。

## 五、法律与许可

- NPatch / LSPatch:LGPL-3.0 谱系(LSPosed)LGPL/GPL 混合,NPatch 仓库为 GPL-3.0;
- 本仓库收录的 `android/compose-miuix/` 文件保留其原始 GPL-3.0 头与移植声明
  (Ported from WeKit; Adapted from compose-miuix-ui IosLiquidGlassNavigationBar);
- 逆向仅用于学习其公开发行 APK 的 UI 实现,并已回链上游源码,未绕过任何保护。
