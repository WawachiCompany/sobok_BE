package com.chihuahua.sobok.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
    org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class
})
@EntityScan("com.chihuahua.sobok")
@EnableJpaRepositories("com.chihuahua.sobok")
@ComponentScan(
    basePackages = "com.chihuahua.sobok",
    includeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.chihuahua\\.sobok\\.account\\..*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.chihuahua\\.sobok\\.member\\..*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.chihuahua\\.sobok\\.routine\\..*")
    },
    useDefaultFilters = false
)
public class TestConfig {

}
