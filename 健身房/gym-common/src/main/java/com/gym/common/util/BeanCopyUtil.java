package com.gym.common.util;

import cn.hutool.core.bean.BeanUtil;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BeanCopyUtil {

    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        return BeanUtil.copyProperties(source, targetClass);
    }

    public static <S, T> T copyProperties(S source, T target) {
        if (source == null || target == null) {
            return target;
        }
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static <S, T> List<T> copyList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceList.stream()
                .map(source -> BeanUtil.copyProperties(source, targetClass))
                .collect(Collectors.toList());
    }
}
