package com.yh.bigdata.tts.common.utils;

import java.awt.*;
import java.util.List;

import com.yh.bigdata.tts.common.model.Trade;
import lombok.extern.slf4j.Slf4j;

import static java.lang.Thread.sleep;

@Slf4j
public final class CommonUtil {

	public static List<Trade> subReverseList(List<Trade> trades, int num) {
		if (trades.size() <= num) {
			return trades;
		}else {
			return trades.subList(trades.size() - num, trades.size());
		}
	}

    public static void beep() {
        try {
            Toolkit.getDefaultToolkit().beep();
            for (int i = 0; i < 5; i++) {
                Toolkit.getDefaultToolkit().beep();
                sleep(200);
            }
        }catch (Exception ex) {
            log.error("beep error...", ex);
        }
    }

}
