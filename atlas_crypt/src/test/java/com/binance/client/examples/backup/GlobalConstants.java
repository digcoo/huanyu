package com.binance.client.examples.backup;

import com.binance.client.enums.PeriodTypeEnum;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class GlobalConstants {
    public static final PeriodTypeEnum FIRST_OP_PERIOD = PeriodTypeEnum.MIN30;

    public static final PeriodTypeEnum SECOND_OP_PERIOD = null;

    //小周期、中周期、大周期
    public static final Map<PeriodTypeEnum, Pair<PeriodTypeEnum, List<PeriodTypeEnum>>> LIMIT_TREND_PERIOD_MAP = ImmutableMap.of(
            PeriodTypeEnum.MIN30, Pair.of(PeriodTypeEnum.HOUR4, Lists.newArrayList(PeriodTypeEnum.HOUR4))
//            PeriodTypeEnum.MIN30, Pair.of(PeriodTypeEnum.HOUR1, Lists.newArrayList(PeriodTypeEnum.HOUR6, PeriodTypeEnum.HOUR8))
//            , PeriodTypeEnum.HOUR1, Pair.of(PeriodTypeEnum.HOUR2, Lists.newArrayList(PeriodTypeEnum.HOUR12, PeriodTypeEnum.DAY1))
//            , PeriodTypeEnum.HOUR2, Pair.of(PeriodTypeEnum.HOUR4, Lists.newArrayList(PeriodTypeEnum.DAY1, PeriodTypeEnum.DAY3))
//            , PeriodTypeEnum.HOUR4, Pair.of(PeriodTypeEnum.HOUR8, Lists.newArrayList(PeriodTypeEnum.DAY3, PeriodTypeEnum.WEEK))
//            , PeriodTypeEnum.HOUR12, Pair.of(PeriodTypeEnum.DAY1, Lists.newArrayList(PeriodTypeEnum.WEEK, PeriodTypeEnum.MONTH))
    );


    public static List<PeriodTypeEnum> getCheckPeriodList(PeriodTypeEnum periodTypeEnum) {
        Set<PeriodTypeEnum> spiderPeriods = new HashSet<>();
        Pair<PeriodTypeEnum, List<PeriodTypeEnum>> periodTypeEnumListPair = GlobalConstants.LIMIT_TREND_PERIOD_MAP.get(periodTypeEnum);
        spiderPeriods.add(periodTypeEnum);
        spiderPeriods.add(periodTypeEnumListPair.getLeft());
        spiderPeriods.addAll(periodTypeEnumListPair.getRight());
        return new ArrayList<>(spiderPeriods);
    }

    public static List<PeriodTypeEnum> getGlobalTrendPeriodList() {
        HashSet<PeriodTypeEnum> trendPeriods = new HashSet<>();
        trendPeriods.add(PeriodTypeEnum.DAY1);
        trendPeriods.add(PeriodTypeEnum.WEEK);
        return new ArrayList<>(trendPeriods);
    }

    public static List<PeriodTypeEnum> getSpiderPeriodList(PeriodTypeEnum periodTypeEnum) {
        if (periodTypeEnum == null) {
            return new ArrayList<>();
        }
        Set<PeriodTypeEnum> spiderPeriods = new HashSet<>();
        spiderPeriods.addAll(getCheckPeriodList(periodTypeEnum));
        spiderPeriods.addAll(getGlobalTrendPeriodList());
        return new ArrayList<>(spiderPeriods);
    }

    public static List<PeriodTypeEnum> getAllPeriodList() {
        Set<PeriodTypeEnum> allPeriodTypes = new HashSet<>();
        allPeriodTypes.addAll(LIMIT_TREND_PERIOD_MAP.keySet());

        LIMIT_TREND_PERIOD_MAP.values().stream().forEach(x -> {
            allPeriodTypes.add(x.getKey());
            allPeriodTypes.addAll(x.getValue());
        });
        return new ArrayList<>(allPeriodTypes);
    }

}
