package com.gym.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 强制使用 AntPathMatcher 进行路径匹配。
 * Spring Boot 2.6+ 默认使用 PathPatternParser，而 Springfox 2.10.5 读取不到 Ant 风格的
 * PatternsRequestCondition，会在启动文档插件时抛 NullPointerException。
 */
@Configuration
public class WebMvcPathMatchConfig implements WebMvcConfigurer, Ordered {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setPatternParser(null);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
