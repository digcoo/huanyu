package com.yh.bigdata.tts.common.dao;

import com.yh.bigdata.tts.common.model.UserWatchlist;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserWatchlistMapper {

    List<UserWatchlist> selectByOpenid(@Param("openid") String openid);

    UserWatchlist selectByOpenidAndStockId(@Param("openid") String openid, @Param("stockId") String stockId);

    int insert(UserWatchlist row);

    int deleteByOpenidAndStockId(@Param("openid") String openid, @Param("stockId") String stockId);
}
