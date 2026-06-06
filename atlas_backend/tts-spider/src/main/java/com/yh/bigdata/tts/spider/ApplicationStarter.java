package com.yh.bigdata.tts.spider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@ServletComponentScan
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApplicationStarter {

	public static void main(String[] args) {
//		SpringApplication.run(ApplicationStarter.class, args);

        new SpringApplicationBuilder(ApplicationStarter.class)
                .headless(false)
                .run(args);
	}
}
