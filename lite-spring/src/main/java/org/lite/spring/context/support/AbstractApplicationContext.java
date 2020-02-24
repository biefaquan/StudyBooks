package org.lite.spring.context.support;

import org.lite.spring.context.ApplicationContext;
import org.lite.spring.core.io.Resource;
import org.lite.spring.beans.factory.support.DefaultBeanFactory;
import org.lite.spring.beans.factory.xml.XmlBeanDefinitionReader;
import org.lite.spring.util.ClassUtils;

/**
 * Created by bfq on 2020/2/17
 */
public abstract class AbstractApplicationContext implements ApplicationContext {
    private DefaultBeanFactory factory = null;
    private ClassLoader beanClassLoader;

    public AbstractApplicationContext(String configFile) {
        factory = new DefaultBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        Resource resource = this.getResourceByPath(configFile);
        reader.loadBeanDefinitions(resource);
        factory.setBeanClassLoader(this.getBeanClassLoader());
    }

    @Override
    public Object getBean(String beanID) {
        return this.factory.getBean(beanID);
    }

    @Override
    public void setBeanClassLoader(ClassLoader beanClassLoader) {
        this.beanClassLoader = beanClassLoader;
    };

    @Override
    public ClassLoader getBeanClassLoader() {
        return (this.beanClassLoader == null ? ClassUtils.getDefaultClassLoader() : this.beanClassLoader);
    };

    protected abstract Resource getResourceByPath(String path);
}