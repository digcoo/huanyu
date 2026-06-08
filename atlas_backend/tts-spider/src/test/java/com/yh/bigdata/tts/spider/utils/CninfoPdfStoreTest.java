package com.yh.bigdata.tts.spider.utils;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class CninfoPdfStoreTest {

    @Test
    public void saveAndLoad_roundTrip() throws Exception {
        Path base = Files.createTempDirectory("cninfo-pdf-test");
        try {
            byte[] pdf = new byte[]{0x25, 0x50, 0x44, 0x46};
            String saved = CninfoPdfStore.save(base.toString(), "sz300750", "2025", pdf);
            Assert.assertNotNull(saved);
            Assert.assertTrue(Files.exists(Path.of(saved)));

            byte[] loaded = CninfoPdfStore.loadIfExists(base.toString(), "sz300750", "2025");
            Assert.assertNotNull(loaded);
            Assert.assertArrayEquals(pdf, loaded);
        } finally {
            Files.walk(base)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (Exception ignored) {
                        }
                    });
        }
    }
}
