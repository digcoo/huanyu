package com.yh.bigdata.tts.common.model;

import lombok.Data;

@Data
public class StockCompanyRelation {

    private Long id;

    private String code;

    private String relatedCode;

    private String relatedName;

    /** competitor / supplier / customer */
    private String relationType;

    private Integer sortOrder;

    private String source;
}
