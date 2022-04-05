
package com.codedream.redis.config;

import com.codedream.redis.serializer.ProtoStuffSerializer;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * ProtoStuff 序列化配置
 *
 * @author yxz
 * @date 2022/03/20
 */
@Configuration
@AutoConfigureBefore(RedisTemplateConfiguration.class)
@ConditionalOnClass(name = "io.protostuff.Schema")
public class ProtoStuffSerializerConfiguration implements CodeDreamRedisSerializerConfigAble {

    @Bean
    @ConditionalOnMissingBean
    @Override
    public RedisSerializer<Object> redisSerializer(CodeDreamRedisProperties properties) {
        if (CodeDreamRedisProperties.SerializerType.ProtoStuff == properties.getSerializerType()) {
            return new ProtoStuffSerializer();
        }
        return defaultRedisSerializer(properties);
    }

}
