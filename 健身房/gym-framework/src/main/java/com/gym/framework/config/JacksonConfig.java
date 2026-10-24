package com.gym.framework.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 全局序列化配置。
 *
 * <p>主键 id、userId 等 Long 类型字段使用雪花算法生成（19 位十进制），
 * 超出 JavaScript Number 的安全整数范围（2^53-1），直接序列化为数字会导致
 * 前端精度丢失，进而使删除/编辑/上下架/状态变更等「按 id 定位」的写操作全部失效。
 *
 * <p>这里统一把 Long/long 序列化为字符串，前端拿到完整 id，写操作恢复正常。
 * 反序列化时前端提交的字符串 id 由 Jackson 自动转回 Long。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> {
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}
