package com.ecommerce.category.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Use strict mapping strategy
        modelMapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT);
        
        return modelMapper;
    }
} 