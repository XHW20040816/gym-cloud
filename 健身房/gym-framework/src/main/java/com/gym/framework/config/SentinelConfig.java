package com.gym.framework.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletResponse;

@Configuration
public class SentinelConfig {

    @Bean
    public BlockExceptionHandler blockExceptionHandler() {
        ObjectMapper objectMapper = new ObjectMapper();
        return (request, response, e) -> {
            response.setStatus(429); // SC_TOO_MANY_REQUESTS (HttpServletResponse 未定义该常量)
            response.setContentType("application/json;charset=UTF-8");
            R<Void> r = R.fail(ResultCode.TOO_MANY_REQUESTS);
            response.getWriter().write(objectMapper.writeValueAsString(r));
        };
    }
}
