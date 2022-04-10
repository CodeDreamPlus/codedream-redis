
package com.codedreamplus.redis.function;

import org.springframework.lang.Nullable;

/**
 * 回调方法
 *
 * @author cool
 */
@FunctionalInterface
public interface CheckedCallBack<T> {


	/**
	 * 执行方法
	 *
	 * @return {@link T}
	 * @throws Throwable throwable
	 */
	@Nullable
	T get() throws Throwable;

}
