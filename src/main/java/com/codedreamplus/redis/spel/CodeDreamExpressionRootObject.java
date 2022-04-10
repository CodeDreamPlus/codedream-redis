package com.codedreamplus.redis.spel;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;

/**
 * ExpressionRootObject
 *
 * @author cool
 */
@Getter
@AllArgsConstructor
public class CodeDreamExpressionRootObject {
	private final Method method;

	private final Object[] args;

	private final Object target;

	private final Class<?> targetClass;

	private final Method targetMethod;
}
