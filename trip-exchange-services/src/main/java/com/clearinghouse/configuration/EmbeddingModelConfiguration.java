package com.clearinghouse.configuration;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EmbeddingModelConfiguration {

    @Bean
    @Primary
    public EmbeddingModel primaryEmbeddingModel(@Qualifier("cohereEmbeddingModel") EmbeddingModel cohereEmbeddingModel) {
        return cohereEmbeddingModel;
    }
}