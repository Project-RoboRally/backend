package dk.dtu.roborally.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Value("${app.cors.allowed-origin-patterns}")
	private String[] allowedOriginPatterns;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**").allowedOriginPatterns(allowedOriginPatterns)
				.allowedMethods("GET", "POST", "OPTIONS")
				.allowedHeaders("Content-Type", "X-XSRF-TOKEN")
				.allowCredentials(true).maxAge(3600);
	}
}
