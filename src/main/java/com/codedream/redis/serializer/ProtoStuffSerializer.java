package com.codedream.redis.serializer;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.util.ObjectUtils;

/**
 * ProtoStuff 序列化
 *
 * @author yxz
 */
public class ProtoStuffSerializer implements RedisSerializer<Object> {
	public static final Schema<BytesWrapper> SCHEMA = RuntimeSchema.getSchema(BytesWrapper.class);

	@Override
	public byte[] serialize(Object object) throws SerializationException {
		if (object == null) {
			return null;
		}
		LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		try {
			return ProtobufIOUtil.toByteArray(new BytesWrapper<>(object), SCHEMA, buffer);
		} finally {
			buffer.clear();
		}
	}

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException {
		if (ObjectUtils.isEmpty(bytes)) {
			return null;
		}
		BytesWrapper<Object> wrapper = new BytesWrapper<>();
		ProtobufIOUtil.mergeFrom(bytes, wrapper, SCHEMA);
		return wrapper.getValue();
	}
}
