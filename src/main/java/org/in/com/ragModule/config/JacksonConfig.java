package org.in.com.ragModule.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@Configuration
public class JacksonConfig {

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer snakeCaseCustomizer() {
		// Applies to ALL RestClient JSON (de)serialization app-wide, including Ollama
		// and Qdrant calls.
		return builder -> builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
	}
}