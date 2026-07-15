# 策略文档（v2）

> 2026-06 起策略体系重构，v1 文档已归档至 [`_archive/`](./_archive/README.md)。  
> 2026-07 nrf / cascade / macedge / wavebreak / wavetier / wavelow / wavetierlow / waveconvex / waveconcave / waveconvexday / waveconcaveday 小程序前端已下线，文档见 [`_archive2/`](./_archive2/README.md)。

| 文档 | 策略 | API | 状态 |
|------|------|-----|------|
| [13.级联MACD凸波段突破.md](./13.级联MACD凸波段突破.md) | 级联 MACD 凸波段同档突破 | `strategy=cascadewaveconvex` | **已实现** |
| [14.级联MACD凹波段突破.md](./14.级联MACD凹波段突破.md) | 级联 MACD 凹波段同档突破 | `strategy=cascadewaveconcave` | **已实现** |
| [15.级联MACD凸波段日突破.md](./15.级联MACD凸波段日突破.md) | 级联 MACD 凸波段日突破 | `strategy=cascadewaveconvexday` | **已实现** |
| [16.级联MACD凹波段日突破.md](./16.级联MACD凹波段日突破.md) | 级联 MACD 凹波段日突破 | `strategy=cascadewaveconcaveday` | **已实现** |
| [19.波段策略.md](./19.波段策略.md) | 波段策略（短线周/中线月 + 日收阳） | `strategy=wavebandShort\|wavebandMedium` | **已实现** |
| [20.多周期波段形态门.md](./20.多周期波段形态门.md) | 凹凸波段突破（日/周/月档 + 凸凹边沿突破） | `strategy=waveperiodgate` | **已实现** |
| [21.同档MACD交叉突破.md](./21.同档MACD交叉突破.md) | 同档 MACD 交叉突破（日/周/月单档） | `strategy=macdcrosstier` | **已实现** |
| [22.凸波段上移.md](./22.凸波段上移.md) | 凸波段上移（日凸 + 周/月/年破前 K high） | `strategy=convexlifttier` | **已实现** |
| [23.柱子策略.md](./23.柱子策略.md) | 柱子策略（日/周/月实体柱突破） | `strategy=bodybar` | **已实现** |
| [24.MACD金叉.md](./24.MACD金叉.md) | MACD金叉（日/周/月末 K 金叉） | `strategy=macdgc` | **已实现** |
| [0.趋势筛选：月周四象限.md](./0.趋势筛选：月周四象限.md) | 趋势筛选（月/周四象限） | `strategy=regime`（草案） | **设计中** |

**说明：** 超短线（`strategy=ultra`）由后端与 `ul*` 参数承载。波段策略：级联 MACD 凸凹 × 同档/日边沿（文档 13–16）；凹/凸波分档突破（文档 17–18）已下线，由文档 19 统一替代。
