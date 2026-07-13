package org.in.com.ragModule.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

@Configuration
public class QdrantConfig {

	@Bean
	public RestClient qdrantRestClient(@Value("${qdrant.base-url}") String baseUrl) {
		JsonMapper qdrantJsonMapper = JsonMapper.builder().propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();

		return RestClient.builder().baseUrl(baseUrl)
				.messageConverters(
						converters -> converters.add(0, new MappingJackson2HttpMessageConverter(qdrantJsonMapper)))
				.build();
	}
}