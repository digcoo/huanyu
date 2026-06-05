package com.yh.bigdata.tts.spider.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	@Autowired
	private Environment env;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
		.allowedOrigins("*")
		.allowedHeaders("*")
		.allowedMethods("*");
	}

	// public void addViewControllers(ViewControllerRegistry registry) {
	// }
	//

	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(new String[] { "swagger-ui.html" })
				.addResourceLocations(new String[] { "classpath:/META-INF/resources/" });
		registry.addResourceHandler(new String[] { "/webjars/**" })
				.addResourceLocations(new String[] { "classpath:/META-INF/resources/webjars/" });
		// registry.addResourceHandler(new String[] { "/static/css/**" })
		// .addResourceLocations(new String[] { "classpath:/dist/static/css/" });
		// registry.addResourceHandler(new String[] { "/static/fonts/**" })
		// .addResourceLocations(new String[] { "classpath:/dist/static/fonts/" });
		// registry.addResourceHandler(new String[] { "/static/img/**" })
		// .addResourceLocations(new String[] { "classpath:/dist/static/img/" });
		// registry.addResourceHandler(new String[] { "/static/js/**" })
		// .addResourceLocations(new String[] { "classpath:/dist/static/js/" });
		// registry.addResourceHandler(new String[] { "/error/**" })
		// .addResourceLocations(new String[] { "classpath:/dist/error/" });
		// registry.addResourceHandler(new String[] { "/login/**" })
		// .addResourceLocations(new String[] { "classpath:/dist/login/" });
		// registry.addResourceHandler(new String[] { "/login/css/**" })
		// .addResourceLocations(new String[] { "classpath:/dist/login/css/" });
		// registry.addResourceHandler(new String[] { "/login/images/**" })
		// .addResourceLocations(new String[] { "classpath:/dist/login/images/" });
		// String filePath = this.env.getProperty("file.path");
		// if (filePath == null || "".equals(filePath)) {
		// filePath = System.getProperty("user.dir");
		// }
		//
		// registry.addResourceHandler(new String[] { "/**" }).addResourceLocations(new
		// String[] { "file:" + filePath });
		super.addResourceHandlers(registry);
	}
	//
	// public void addInterceptors(InterceptorRegistry registry) {
	// registry.addInterceptor(new UrlInterceptor()).addPathPatterns(new String[] {
	// "/**" });
	// super.addInterceptors(registry);
	// }
}
