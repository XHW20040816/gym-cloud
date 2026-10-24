package com.gym.framework.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    private static final Long DEFAULT_USER_ID = 1L;

    /**
     * 获取当前登录用户ID，默认1L。真实项目从安全上下文/ThreadLocal获取。
     */
    private Long currentUserId() {
        return DEFAULT_USER_ID;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("start insert fill ....");
        LocalDateTime now = LocalDateTime.now();
        Long userId = currentUserId();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createBy", Long.class, userId);
        this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("start update fill ....");
        Long userId = currentUserId();
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
    }
}
