package com.yh.bigdata.tts.common.dao;

import com.yh.bigdata.tts.common.model.UserWatchHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserWatchHistoryMapper {

    List<UserWatchHistory> selectByOpenid(@Param("openid") String openid, @Param("filter") String filter);

    int insert(UserWatchHistory row);

    int countByOpenid(@Param("openid") String openid);

    int deleteOldestBeyond(@Param("openid") String openid, @Param("keep") int keep);
}
