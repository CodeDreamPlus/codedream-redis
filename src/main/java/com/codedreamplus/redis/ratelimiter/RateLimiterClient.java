package com.codedreamplus.redis.ratelimiter;


import com.codedreamplus.redis.function.CheckedCallBack;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

/**
 * 限流客户端
 * RateLimiter 限流 Client
 *
 * @author cool
 * @date 2022/04/01
 */
public interface RateLimiterClient {

    /**
     * 服务是否被限流
     *
     * @param key 自定义的key，请保证唯一
     * @param max 支持的最大请求
     * @param ttl 时间,单位默认为秒（seconds）
     * @return 是否允许
     */
    default boolean isAllowed(String key, long max, long ttl) {
        return this.isAllowed(key, max, ttl, TimeUnit.SECONDS);
    }

    /**
     * 服务是否被限流
     *
     * @param key      自定义的key，请保证唯一
     * @param max      支持的最大请求
     * @param ttl      时间
     * @param timeUnit 时间单位
     * @return 是否允许
     */
    boolean isAllowed(String key, long max, long ttl, TimeUnit timeUnit);

    /**
     * 服务限流，被限制时抛出 RateLimiterException 异常，需要自行处理异常
     *
     * @param key      自定义的key，请保证唯一
     * @param max      支持的最大请求
     * @param ttl      时间
     * @param supplier Supplier 函数式
     * @return 函数执行结果
     */
    default <T> T allow(String key, long max, long ttl, CheckedCallBack<T> supplier) {
        return allow(key, max, ttl, TimeUnit.SECONDS, supplier);
    }

    /**
     * 服务限流，被限制时抛出 RateLimiterException 异常，需要自行处理异常
     *
     * @param key             自定义的key，请保证唯一
     * @param max             支持的最大请求
     * @param ttl             时间
     * @param timeUnit        时间单位
     * @param checkedCallBack 函数式
     * @param <T>
     * @return 函数执行结果
     */
    default <T> T allow(String key, long max, long ttl, TimeUnit timeUnit, CheckedCallBack<T> checkedCallBack) {
        boolean isAllowed = this.isAllowed(key, max, ttl, timeUnit);
        if (isAllowed) {
            try {
                return checkedCallBack.get();
            } catch (Throwable e) {
                if (e instanceof Error) {
                    throw (Error) e;
                } else if (e instanceof IllegalAccessException ||
                        e instanceof IllegalArgumentException ||
                        e instanceof NoSuchMethodException) {
                    throw new IllegalArgumentException(e);
                } else if (e instanceof InvocationTargetException) {
                    throw new RuntimeException(((InvocationTargetException) e).getTargetException());
                } else if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                throw new RuntimeException(e);
            }
        }
        throw new RateLimiterException(key, max, ttl, timeUnit);
    }
}
