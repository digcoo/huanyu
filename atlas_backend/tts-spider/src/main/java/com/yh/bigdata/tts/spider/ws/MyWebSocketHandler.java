package com.yh.bigdata.tts.spider.ws;

import com.alibaba.fastjson.JSON;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockTarget;
import com.yh.bigdata.tts.spider.service.StrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    // 线程安全的连接池
    private static final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Autowired
    private StrategyService strategyService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("客户端连接: {}", session.getId());
        sendMessage(session, "Welcome! Your ID: " + session.getId());

        List<StockTarget> stockTargetList = strategyService.getAndUpdateTriggerStockTargets(StrategyTypeEnum.TREND_NEW);
        if(CollectionUtils.isEmpty(stockTargetList)) {
            return;
        }

        int limit = 100;
        Iterator<StockTarget> iterator = stockTargetList.iterator();
        while (iterator.hasNext() && limit > 0){
            sendMessage(session, JSON.toJSONString(iterator.next()));
            limit--;
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.info("收到消息: {}", payload);

        // 心跳检测（客户端发送ping）
        if ("ping".equals(payload)) {
            sendMessage(session, "pong");
            return;
        }

        // 广播消息给所有客户端
        broadcast("[" + session.getId() + "]: " + payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("客户端断开: {}", session.getId());
    }

    // 单发消息
    public void sendMessage(WebSocketSession session, String text) {
        try {
            session.sendMessage(new TextMessage(text));
        } catch (Exception e) {
            log.error("Error sending message to {}",session.getId(), e);
        }
    }

    // 广播消息
    public void broadcast(String text) {
        sessions.forEach(session -> sendMessage(session, text));
    }
}
