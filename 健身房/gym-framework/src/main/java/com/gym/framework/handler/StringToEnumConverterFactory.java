package com.gym.framework.handler;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 字符串转枚举通用工厂。
 * 优先尝试 valueOf/code/fromValue/fromCode 等静态方法。
 */
@Component
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum<?>> {

    @Override
    public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return source -> {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }
            // 1) 直接按枚举名
            try {
                return (T) Enum.valueOf(targetType.asSubclass(Enum.class), source);
            } catch (IllegalArgumentException ignored) {
            }

            // 2) 尝试通过静态方法 fromCode / fromValue / getInstance 或静态字段 code
            for (String methodName : new String[]{"fromCode", "fromValue", "getByCode", "getByValue"}) {
                try {
                    Method m = targetType.getMethod(methodName, String.class);
                    Object result = m.invoke(null, source);
                    if (result != null) {
                        return (T) result;
                    }
                } catch (NoSuchMethodException ignored) {
                } catch (Exception ignored) {
                }
            }
            // 3) 尝试 Integer code 的 fromCode(int) / valueOf(int)
            if (source.matches("-?\\d+")) {
                for (String methodName : new String[]{"fromCode", "fromValue", "getByCode", "valueOf"}) {
                    try {
                        Method m = targetType.getMethod(methodName, Integer.class);
                        Object result = m.invoke(null, Integer.parseInt(source));
                        if (result != null) {
                            return (T) result;
                        }
                    } catch (NoSuchMethodException ignored) {
                    } catch (Exception ignored) {
                    }
                }
                try {
                    Method m = targetType.getMethod("getByCode", int.class);
                    Object result = m.invoke(null, Integer.parseInt(source));
                    if (result != null) {
                        return (T) result;
                    }
                } catch (Exception ignored) {
                }
            }
            throw new IllegalArgumentException("无法将字符串 '" + source + "' 转换为枚举 " + targetType.getName());
        };
    }
}
