package com.jenetics.smocker.util.lang;

import java.lang.reflect.ParameterizedType;

public class ReflectionUtil {
	
	public static Class<?> findSuperClassParameterType(Object instance, Class<?> classOfInterest, int parameterIndex) {
		Class<?> subClass = instance.getClass();
		classOfInterest.isAssignableFrom(instance.getClass());
		while (subClass != subClass.getSuperclass()) {
			// instance.getClass() is no subclass of classOfInterest or instance is a direct instance of classOfInterest
			subClass = subClass.getSuperclass();
			if (subClass == null) throw new IllegalArgumentException();
		}
		ParameterizedType parameterizedType = (ParameterizedType) subClass.getGenericSuperclass();
		return (Class<?>) parameterizedType.getActualTypeArguments()[parameterIndex];
	}
}
