package org.lite.spring.beans.factory.support;

import org.lite.spring.beans.factory.config.SingletonBeanRegistry;
import org.lite.spring.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {
	
	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<String, Object>(64);

	@Override
	public void registerSingleton(String beanName, Object singletonObject) {
		
		Assert.notNull(beanName, "'beanName' must not be null");
		
		Object oldObject = this.singletonObjects.get(beanName);
		if (oldObject != null) {
			throw new IllegalStateException("Could not register object [" + singletonObject +
					"] under bean name '" + beanName + "': there is already object [" + oldObject + "] bound");
		}
		this.singletonObjects.put(beanName, singletonObject);
		
	}

	@Override
	public Object getSingleton(String beanName) {
		
		return this.singletonObjects.get(beanName);
	}

}
