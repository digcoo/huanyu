package com.yh.bigdata.tts.spider.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 巨潮年报 PDF 本地暂存：{baseDir}/{code}/{reportYear}.pdf
 */
public final class CninfoPdfStore {

    private static final Logger log = LoggerFactory.getLogger(CninfoPdfStore.class);

    private CninfoPdfStore() {
    }

    public static Path resolveFile(String baseDir, String code, String reportYear) {
        if (StringUtils.isBlank(baseDir) || StringUtils.isBlank(code)) {
            return null;
        }
        String year = StringUtils.isNotBlank(reportYear) ? reportYear : "unknown";
        return Paths.get(baseDir, code.toLowerCase(), year + ".pdf");
    }

    public static byte[] loadIfExists(String baseDir, String code, String reportYear) {
        Path file = resolveFile(baseDir, code, reportYear);
        if (file == null || !Files.isRegularFile(file)) {
            return null;
        }
        try {
            byte[] data = Files.readAllBytes(file);
            if (data.length == 0) {
                return null;
            }
            log.debug("cninfo pdf cache hit, path={}", file);
            return data;
        } catch (IOException e) {
            log.warn("cninfo pdf cache read failed, path={}", file, e);
            return null;
        }
    }

    public static String save(String baseDir, String code, String reportYear, byte[] pdfBytes) {
        if (StringUtils.isBlank(baseDir) || pdfBytes == null || pdfBytes.length == 0) {
            return null;
        }
        Path file = resolveFile(baseDir, code, reportYear);
        if (file == null) {
            return null;
        }
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, pdfBytes);
            log.debug("cninfo pdf saved, path={}, bytes={}", file, pdfBytes.length);
            return file.toAbsolutePath().toString();
        } catch (IOException e) {
            log.warn("cninfo pdf save failed, path={}", file, e);
            return null;
        }
    }
}
