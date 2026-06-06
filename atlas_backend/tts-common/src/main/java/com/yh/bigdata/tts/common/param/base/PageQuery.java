package com.yh.bigdata.tts.common.param.base;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author duyp
 * 
 * @date 2019/01/15
 * 
 * @comment 分页参数
 */
@Data
@NoArgsConstructor
public class PageQuery {
	private Integer page = 1;
	private Integer size = 10;
	private String orderBy = "create_time desc";

    public PageQuery(Integer page, Integer size) {
    	this.page = page;
    	this.size = size;
    }

}
