package com.codedream.redis.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * redis cache 扩展cache name自动化配置
 *
 * @author yxz
 */
public class RedisAutoCacheManager extends RedisCacheManager {

	public RedisAutoCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration,
								 Map<String, RedisCacheConfiguration> initialCacheConfigurations, boolean allowInFlightCacheCreation) {
		super(cacheWriter, defaultCacheConfiguration, initialCacheConfigurations, allowInFlightCacheCreation);
	}

	@Override
	protected RedisCache createRedisCache(String name, @Nullable RedisCacheConfiguration cacheConfig) {
		if (StringUtils.isBlank(name) || !name.contains("#")) {
			return super.createRedisCache(name, cacheConfig);
		}
		String[] cacheArray = name.split("#");
		if (cacheArray.length < 2) {
			return super.createRedisCache(name, cacheConfig);
		}
		String cacheName = cacheArray[0];
		if (cacheConfig != null) {
			Duration cacheAge = DurationStyle.detectAndParse(cacheArray[1], ChronoUnit.SECONDS);;
			cacheConfig = cacheConfig.entryTtl(cacheAge);
		}
		return super.createRedisCache(cacheName, cacheConfig);
	}

}
