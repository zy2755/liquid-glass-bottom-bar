# 03 · Web:liquid-glass-web 技术移植到底栏

> 折射引擎逐字源自 [shuding/liquid-glass](https://github.com/shuding/liquid-glass)(MIT),
> 扩展与文档来自 [xiaofangstudio/liquid-glass-web](https://github.com/xiaofangstudio/liquid-glass-web)(MIT)。
> 本仓库 [`web/bottombar-liquid-glass.html`](../web/bottombar-liquid-glass.html) 把该技术第一次适配成**底栏**(原项目只做了可拖动按钮)。

## 原理(不用 WebGL)

```
CSS backdrop-filter: url(#svgFilter) blur(0.25px) contrast(1.2) brightness(1.05) saturate(1.1)
                          │
SVG feDisplacementMap(位移贴图)  ←  feImage(运行时 Canvas 生成)
                                        │
                            SDF → smoothStep → 位移场 → 归一化成贴图
```

核心三步:

1. **算位移场**:`内缩矩形 SDF → smoothStep 得强度 → 位移向量`;
2. **生成贴图**:位移场归一化后写进 128~256px 的 Canvas,作为 `feImage`;
3. **折射**:`feDisplacementMap` 用贴图弯曲真实背景(R 通道控制 x、G 通道控制 y)。

## SDF fragment(原版参数,底栏直接可用)

```js
function fragment(uv) {                       // uv ∈ [0,1]
  const ix = uv.x - 0.5, iy = uv.y - 0.5;     // 居中坐标
  const distanceToEdge = roundedRectSDF(ix, iy, 0.3, 0.2, 0.6);
  const displacement = smoothStep(0.8, 0, distanceToEdge - 0.15);
  const scaled = smoothStep(0, 1, displacement);
  return { x: ix * scaled + 0.5, y: iy * scaled + 0.5 };
}
```

- **SDF 必须内缩(0.3 / 0.2)**:若写 0.5/0.5 撑满元素,位移场恒为 1,归一化后 scale≈0,
  折射彻底消失(变"塑料片")—— liquid-glass-web 称之为血泪陷阱 #1;
- **按压加厚**:阈值 `-0.15 / strength`(按下 strength=1.3、松开 1)重建贴图;
- **归一化**:`maxScale = max(|raw|) · 0.5`(×0.5 折半是原版手感,勿删);
  贴图像素 = `raw/maxScale + 0.5` 再 ×255;
- **feDisplacementMap 的 scale** = `maxScale / devicePixelRatio`;
- 元素尺寸/DPR 变化时**重建贴图**(resize 监听)。

## 底栏适配要点(相对原项目的改动)

1. **形状**:底栏是"宽而扁的胶囊",SDF 的内缩半宽/半高要按真实宽高比算:
   `halfW = (w/2 - inset) / w`、`halfH = (h/2 - inset) / h`(用 inset≈2~6px 而不是固定 0.3/0.2,
   否则扁长元素上 0.3·width 的内缩会把折射带挤没了);圆角半径 = 半高(pill)。
2. **定位**:`position: fixed; bottom: calc(12px + env(safe-area-inset-bottom)); left: 50%; translateX(-50%)`,
   让内容自然延伸到玻璃之下(与 Android 的"血泪经验"同一原则)。
3. **贴图生成按元素尺寸**(不是固定 0.3/0.2 的单位方形),高度 64px、宽度自适应。
4. **降级**:`@supports not (backdrop-filter: url(#f))` 或非 Chromium →
   换成普通毛玻璃 `background: rgba(255,255,255,.55); backdrop-filter: blur(20px);`。
5. **交互动效**(原项目参数,可直接用于底栏):
   - 按压:`scale(1.05, 0.93)` + 内阴影加深;
   - 回弹:500ms,`springEase(t) = 1 − e^(−5t)·cos(9t)`(一个过冲周期);
   - 玻璃表面:`background: rgba(255,255,255,0.08)`(偏白不偏灰)+
     `box-shadow: 0 4px 8px rgba(0,0,0,.25), 0 -10px 25px inset rgba(0,0,0,.15)`(底部内阴影出"厚"感)。
6. **tab 指示器**:Web 版若要复刻 Android 三层结构,可再嵌一个内层元素,
   把 tab 文字用 `element()`/截图方式喂给指示器的滤镜 —— 兼容性差,建议只做"背景折射 + 高光滑动"。

## 兼容性

| 浏览器 | 效果 |
|---|---|
| Chrome / Edge / 其他 Chromium | 完整折射 |
| Firefox | `backdrop-filter: url(#svg)` 不支持 → 降级普通毛玻璃(需写好 fallback) |
| Safari | 支持部分 backdrop-filter,`url(#svg)` 不稳定 → 同样降级 |

移动端记得 `touch-action: none` + Pointer Events(若玻璃可拖)。
