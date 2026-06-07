package com.yh.bigdata.tts.spider.crawler.detail;

import com.alibaba.fastjson.JSONObject;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockCompanyRelationMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockCompanyRelation;
import com.yh.bigdata.tts.common.utils.StockCodeUtil;
import com.yh.bigdata.tts.spider.utils.EastMoneyF10Client;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 公司详情爬取：主营业务、行业、估值、竞品
 */
@Component
public class StockCompanyDetailCrawler {

    private static final Logger log = LoggerFactory.getLogger(StockCompanyDetailCrawler.class);

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Autowired
    private StockBaseMapper stockBaseMapper;

    @Autowired
    private StockCompanyRelationMapper stockCompanyRelationMapper;

    @Value("${atlas.spider.detail.interval-ms:250}")
    private long intervalMs;

    public boolean isRunning() {
        return running.get();
    }

    public void runAll() {
        if (!running.compareAndSet(false, true)) {
            log.warn("StockCompanyDetailCrawler already running, skip");
            return;
        }
        long start = System.currentTimeMillis();
        log.info("StockCompanyDetailCrawler ALL start");
        try {
            List<StockBase> all = stockBaseMapper.selectAll();
            int ok = 0;
            int fail = 0;
            for (StockBase stock : all) {
                if (stock == null || StringUtils.isBlank(stock.getCode())) {
                    continue;
                }
                try {
                    if (runOne(stock.getCode(), stock.getName())) {
                        ok++;
                    } else {
                        fail++;
                    }
                    sleepInterval();
                } catch (Exception e) {
                    fail++;
                    log.warn("company detail crawl failed, code={}", stock.getCode(), e);
                }
            }
            log.info("StockCompanyDetailCrawler ALL done, ok={}, fail={}, cost={}s",
                    ok, fail, (System.currentTimeMillis() - start) / 1000);
        } finally {
            running.set(false);
        }
    }

    public boolean runOne(String code, String name) {
        String normalized = StockCodeUtil.normalizeCnCode(code);
        if (StringUtils.isBlank(normalized)) {
            return false;
        }

        JSONObject survey = EastMoneyF10Client.fetchCompanySurvey(normalized);
        JSONObject quote = EastMoneyF10Client.fetchQuote(normalized);
        List<JSONObject> competitors = EastMoneyF10Client.fetchCompetitors(normalized);

        if (survey == null && quote == null && competitors.isEmpty()) {
            return false;
        }

        StockBase patch = new StockBase();
        patch.setCode(normalized);

        if (survey != null) {
            String industry = EastMoneyF10Client.parseIndustry(survey.getString("EM2016"));
            patch.setIndustry(industry);
            patch.setIndustryCsrc(EastMoneyF10Client.trimText(survey.getString("INDUSTRYCSRC1"), 128));
            patch.setQuancheng(StringUtils.defaultIfBlank(survey.getString("ORG_NAME"), name));
            patch.setOrgProfile(EastMoneyF10Client.trimText(survey.getString("ORG_PROFILE"), 4000));
            String business = EastMoneyF10Client.trimText(survey.getString("BUSINESS_SCOPE"), 800);
            if (StringUtils.isBlank(business)) {
                business = EastMoneyF10Client.trimText(survey.getString("ORG_PROFILE"), 300);
            }
            patch.setMainBusiness(business);
        }

        if (quote != null) {
            patch.setPeTtm(EastMoneyF10Client.scale100(quote.get("f162")));
            patch.setPb(EastMoneyF10Client.scale100(quote.get("f167")));
            patch.setDividendYield(EastMoneyF10Client.scale100(quote.get("f168")));
            patch.setHigh52w(EastMoneyF10Client.scale100(quote.get("f46")));
            patch.setLow52w(EastMoneyF10Client.scale100(quote.get("f45")));
            patch.setTotalMvYi(EastMoneyF10Client.toYi(quote.get("f116")));
        }

        stockBaseMapper.updateByPrimaryKeySelective(patch);
        saveCompetitors(normalized, competitors);
        return true;
    }

    private void saveCompetitors(String code, List<JSONObject> competitors) {
        stockCompanyRelationMapper.deleteByCode(code);
        if (competitors.isEmpty()) {
            return;
        }
        List<StockCompanyRelation> rows = new ArrayList<>();
        int order = 0;
        for (JSONObject row : competitors) {
            if (row == null) {
                continue;
            }
            String relatedNum = row.getString("CORRE_SECURITY_CODE");
            if (StringUtils.isBlank(relatedNum)) {
                continue;
            }
            String relatedCode = relatedNum.startsWith("6") ? "sh" + relatedNum : "sz" + relatedNum;
            StockCompanyRelation rel = new StockCompanyRelation();
            rel.setCode(code);
            rel.setRelatedCode(relatedCode);
            rel.setRelatedName(row.getString("CORRE_SECURITY_NAME"));
            rel.setRelationType("competitor");
            rel.setSortOrder(order++);
            rel.setSource("eastmoney");
            rows.add(rel);
            if (rows.size() >= 10) {
                break;
            }
        }
        if (!rows.isEmpty()) {
            stockCompanyRelationMapper.insertBatch(rows);
        }
    }

    private void sleepInterval() {
        if (intervalMs <= 0) {
            return;
        }
        try {
            Thread.sleep(intervalMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
