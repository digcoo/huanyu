package com.yh.bigdata.tts.common.dao;

import com.yh.bigdata.tts.common.model.AtlasUser;
import org.apache.ibatis.annotations.Param;

public interface AtlasUserMapper {

    int upsert(AtlasUser user);

    AtlasUser selectByOpenid(@Param("openid") String openid);
}
