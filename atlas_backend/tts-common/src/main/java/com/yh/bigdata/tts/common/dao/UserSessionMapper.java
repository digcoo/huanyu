package com.yh.bigdata.tts.common.dao;

import com.yh.bigdata.tts.common.model.UserSession;
import org.apache.ibatis.annotations.Param;

public interface UserSessionMapper {

    int insert(UserSession session);

    UserSession selectByToken(@Param("token") String token);

    int deleteByToken(@Param("token") String token);
}
