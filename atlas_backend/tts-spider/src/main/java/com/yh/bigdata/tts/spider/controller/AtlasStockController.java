package com.yh.bigdata.tts.spider.controller;

import com.yh.bigdata.tts.common.dto.atlas.AtlasCompassModuleVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasKlineBarVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasMarketIndexVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasDc2MarkersVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasGc2MarkersVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasRetestMarkersVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasUlowMin30MarkersVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasStockDetailVo;
import com.yh.bigdata.tts.common.dto.atlas.AtlasStockSummaryVo;
import com.yh.bigdata.tts.common.param.FrictionlessLadderStrategyParams;
import com.yh.bigdata.tts.common.param.StockPageQuery;
import com.yh.bigdata.tts.common.param.LongStrategyParams;
import com.yh.bigdata.tts.common.param.MediumStrategyParams;
import com.yh.bigdata.tts.common.param.TrendV2StrategyParams;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.common.param.base.Response;
import com.yh.bigdata.tts.common.param.base.ResponseUtil;
import com.yh.bigdata.tts.spider.service.AtlasStockApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/stock")
@Slf4j
public class AtlasStockController {

    @Autowired
    private AtlasStockApiService atlasStockApiService;

    @GetMapping("/health")
    public Response<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "ok");
        data.put("cacheReady", atlasStockApiService.isCacheReady());
        return ResponseUtil.success(data);
    }

    @GetMapping("/search")
    public Response<List<AtlasStockSummaryVo>> search(
            @RequestParam("q") String keyword,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return ResponseUtil.success(atlasStockApiService.search(keyword, limit));
    }

    @GetMapping("/indices")
    public Response<List<AtlasMarketIndexVo>> getIndices(
            @RequestParam(value = "market", defaultValue = "cn") String market,
            @RequestParam(value = "period", defaultValue = "week") String period,
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        return ResponseUtil.success(atlasStockApiService.getMarketIndices(market, period, limit));
    }

    @GetMapping("/{code}")
    public Response<AtlasStockSummaryVo> getStock(@PathVariable("code") String code) {
        try {
            return ResponseUtil.success(atlasStockApiService.getSummary(code));
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/klines")
    public Response<List<AtlasKlineBarVo>> getKlines(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "week") String period,
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        try {
            return ResponseUtil.success(atlasStockApiService.getKlines(code, period, limit));
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @PostMapping("/{code}/klines/refresh")
    public Response<List<AtlasKlineBarVo>> refreshKlines(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "week") String period,
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        try {
            List<AtlasKlineBarVo> bars = atlasStockApiService.refreshKlines(code, period, limit);
            if (bars.isEmpty()) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(bars);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping({"/{code}/ladder/markers", "/{code}/ulow/markers"})
    public Response<AtlasUlowMin30MarkersVo> getLadderMin30Markers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "min30") String period) {
        try {
            AtlasUlowMin30MarkersVo markers = atlasStockApiService.getLadderMarkers(code, period);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/ultra/markers")
    public Response<AtlasUlowMin30MarkersVo> getUltraMin30Markers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "min30") String period,
            StockPageQuery query) {
        try {
            UltraShortStrategyParams params = query != null
                    ? query.toUltraShortParams()
                    : UltraShortStrategyParams.defaults();
            AtlasUlowMin30MarkersVo markers = atlasStockApiService.getUltraMarkers(code, period, params);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/trend/markers")
    public Response<AtlasUlowMin30MarkersVo> getTrendDayMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period,
            StockPageQuery query) {
        try {
            TrendV2StrategyParams params = query != null
                    ? query.toTrendV2Params()
                    : TrendV2StrategyParams.defaults();
            AtlasUlowMin30MarkersVo markers = atlasStockApiService.getTrendMarkers(code, period, params);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/medium/markers")
    public Response<AtlasUlowMin30MarkersVo> getMediumWeekMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "week") String period,
            StockPageQuery query) {
        try {
            MediumStrategyParams params = query != null
                    ? query.toMediumParams()
                    : MediumStrategyParams.defaults();
            AtlasUlowMin30MarkersVo markers = atlasStockApiService.getMediumMarkers(code, period, params);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/long/markers")
    public Response<AtlasUlowMin30MarkersVo> getLongMonthMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "month") String period,
            StockPageQuery query) {
        try {
            LongStrategyParams params = query != null
                    ? query.toLongParams()
                    : LongStrategyParams.defaults();
            AtlasUlowMin30MarkersVo markers = atlasStockApiService.getLongMarkers(code, period, params);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/retest/markers")
    public Response<AtlasRetestMarkersVo> getRetestMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period) {
        try {
            AtlasRetestMarkersVo markers = atlasStockApiService.getRetestMarkers(code, period);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/gc2/markers")
    public Response<AtlasGc2MarkersVo> getGc2Markers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period) {
        try {
            AtlasGc2MarkersVo markers = atlasStockApiService.getGc2Markers(code, period);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/cascade/markers")
    public Response<AtlasGc2MarkersVo> getCascadeMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period) {
        try {
            AtlasGc2MarkersVo markers = atlasStockApiService.getCascadeMarkers(code, period);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/ldip/markers")
    public Response<AtlasGc2MarkersVo> getLdipMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period) {
        try {
            AtlasGc2MarkersVo markers = atlasStockApiService.getLdipMarkers(code, period);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/macedge/markers")
    public Response<AtlasGc2MarkersVo> getMacdEdgeMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period) {
        try {
            AtlasGc2MarkersVo markers = atlasStockApiService.getMacdEdgeMarkers(code, period);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/waveconvex/markers")
    public Response<AtlasGc2MarkersVo> getWaveconvexMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period) {
        try {
            AtlasGc2MarkersVo markers = atlasStockApiService.getWaveconvexMarkers(code, period);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/waveconcave/markers")
    public Response<AtlasGc2MarkersVo> getWaveconcaveMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period) {
        try {
            AtlasGc2MarkersVo markers = atlasStockApiService.getWaveconcaveMarkers(code, period);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/waveconvexday/markers")
    public Response<AtlasGc2MarkersVo> getWaveconvexdayMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period) {
        try {
            AtlasGc2MarkersVo markers = atlasStockApiService.getWaveconvexdayMarkers(code, period);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/waveconcaveday/markers")
    public Response<AtlasGc2MarkersVo> getWaveconcavedayMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period) {
        try {
            AtlasGc2MarkersVo markers = atlasStockApiService.getWaveconcavedayMarkers(code, period);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/cascadewaveconvex/markers")
    public Response<AtlasGc2MarkersVo> getCascadewaveconvexMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period) {
        try {
            AtlasGc2MarkersVo markers = atlasStockApiService.getCascadewaveconvexMarkers(code, period);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/cascadewaveconcave/markers")
    public Response<AtlasGc2MarkersVo> getCascadewaveconcaveMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period) {
        try {
            AtlasGc2MarkersVo markers = atlasStockApiService.getCascadewaveconcaveMarkers(code, period);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/waveband/markers")
    public Response<AtlasGc2MarkersVo> getWavebandMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period) {
        try {
            AtlasGc2MarkersVo markers = atlasStockApiService.getWavebandMarkers(code, period);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/cascadewaveconvexday/markers")
    public Response<AtlasGc2MarkersVo> getCascadewaveconvexdayMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period) {
        try {
            AtlasGc2MarkersVo markers = atlasStockApiService.getCascadewaveconvexdayMarkers(code, period);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/cascadewaveconcaveday/markers")
    public Response<AtlasGc2MarkersVo> getCascadewaveconcavedayMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period) {
        try {
            AtlasGc2MarkersVo markers = atlasStockApiService.getCascadewaveconcavedayMarkers(code, period);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/nrf/markers")
    public Response<AtlasGc2MarkersVo> getNrfMarkers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period,
            StockPageQuery query) {
        try {
            FrictionlessLadderStrategyParams nrfParams = query != null
                    ? query.toFrictionlessParams()
                    : FrictionlessLadderStrategyParams.defaults();
            TrendV2StrategyParams trendParams = query != null
                    ? query.toTrendV2Params()
                    : TrendV2StrategyParams.defaults();
            MediumStrategyParams mediumParams = query != null
                    ? query.toMediumParams()
                    : MediumStrategyParams.defaults();
            LongStrategyParams longParams = query != null
                    ? query.toLongParams()
                    : LongStrategyParams.defaults();
            AtlasGc2MarkersVo markers = atlasStockApiService.getNrfMarkers(
                    code, period, nrfParams, trendParams, mediumParams, longParams);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/dc2/markers")
    public Response<AtlasDc2MarkersVo> getDc2Markers(
            @PathVariable("code") String code,
            @RequestParam(value = "period", defaultValue = "day") String period) {
        try {
            AtlasDc2MarkersVo markers = atlasStockApiService.getDc2Markers(code, period);
            if (markers == null) {
                return ResponseUtil.fail(ResponseUtil.NO_DATA);
            }
            return ResponseUtil.success(markers);
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/detail")
    public Response<AtlasStockDetailVo> getDetail(@PathVariable("code") String code) {
        try {
            return ResponseUtil.success(atlasStockApiService.getDetail(code));
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }

    @GetMapping("/{code}/compass")
    public Response<Map<String, AtlasCompassModuleVo>> getCompass(@PathVariable("code") String code) {
        try {
            return ResponseUtil.success(atlasStockApiService.getCompass(code));
        } catch (NoSuchElementException ex) {
            return ResponseUtil.fail(ResponseUtil.NO_DATA);
        }
    }
}
