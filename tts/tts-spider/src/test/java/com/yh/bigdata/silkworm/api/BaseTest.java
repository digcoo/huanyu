package com.yh.bigdata.silkworm.api;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.yh.bigdata.tts.spider.ApplicationStarter;

/**
 * @author duyp
 * 
 * @date 2019/01/29
 * 
 * @comment
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationStarter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseTest {

    @LocalServerPort
    private int port;

    private URL baseUrl;

    protected MockMvc mvc;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private WebApplicationContext context;

    @Before
    public void setupMockMvc() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
    
    @Before
    public void setUp() throws Exception {
        String url = String.format("http://localhost:%d/tts", port);
        System.out.println(String.format("port is : [%d]", port));
        this.baseUrl = new URL(url);
        
        //login
    }

	public TestRestTemplate getRestTemplate() {
		return restTemplate;
	}

	public String getBaseUrl() {
		return baseUrl.toString();
	}
	
	@Test
	public void test() {
		System.out.println("-=====");
	}
    
}
