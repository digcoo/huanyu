package com.yh.bigdata.tts.spider.utils;

import org.junit.Assert;
import org.junit.Test;

public class CninfoTradingPartiesParserTest {

    @Test
    public void parseText_extractsCustomersAndSuppliers() {
        String text = "主要销售客户和主要供应商情况 \n"
                + "1）公司主要销售客户情况 \n"
                + "公司前 5 大客户资料 \n"
                + "序号 客户名称 销售额（千元） 占年度销售总额比例 \n"
                + "1 第一名 54,173,399 14.96% \n"
                + "2 第二名 27,868,873 7.70% \n"
                + "3 第三名 22,441,092 6.20% \n"
                + "4 第四名 17,447,788 4.82% \n"
                + "5 第五名 12,133,080 3.35% \n"
                + "合计 -- 134,064,232 37.03% \n"
                + "2）公司主要供应商情况 \n"
                + "公司前 5 名供应商资料 \n"
                + "序号 供应商名称 采购额（千元） 占年度采购总额比例 \n"
                + "1  第一名                             16,264,222  5.99% \n"
                + "2  第二名                               9,058,659  3.34% \n"
                + "3  第三名                               8,218,966  3.03% \n"
                + "4  第四名                               5,781,185  2.13% \n"
                + "5  第五名                               5,019,088  1.85% \n"
                + "合计 --                             44,342,120  16.33% \n"
                + "3、费用 \n";

        CninfoTradingPartiesParser.ParseResult result = CninfoTradingPartiesParser.parseText(text);
        Assert.assertEquals(5, result.getCustomers().size());
        Assert.assertEquals(5, result.getSuppliers().size());
        Assert.assertEquals("第一名", result.getCustomers().get(0).getName());
        Assert.assertEquals("14.96", result.getCustomers().get(0).getRatioPct());
        Assert.assertEquals("541.73亿", result.getCustomers().get(0).displayLabel().split(" · ")[1]);
        Assert.assertEquals("第一名", result.getSuppliers().get(0).getName());
    }
}
