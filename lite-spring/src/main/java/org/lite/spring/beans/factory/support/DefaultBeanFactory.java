package org.lite.spring.beans.factory.support;

import org.apache.commons.beanutils.BeanUtils;
import org.lite.spring.beans.BeanDefinition;
import org.lite.spring.beans.PropertyValue;
import org.lite.spring.beans.SimpleTypeConverter;
import org.lite.spring.beans.factory.BeanCreationException;
import org.lite.spring.beans.factory.config.ConfigurableBeanFactory;
import org.lite.spring.util.ClassUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bfq on 2020/2/13
 */
public class DefaultBeanFactory extends DefaultSingletonBeanRegistry
        implements BeanDefinitionRegistry, ConfigurableBeanFactory {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>(64);

    private ClassLoader beanClassLoader;

    public DefaultBeanFactory() {}

    @Override
    public void registerBeanDefinition(String beanID,BeanDefinition bd){
        this.beanDefinitionMap.put(beanID, bd);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanID) {

        return this.beanDefinitionMap.get(beanID);
    }

    @Override
    public Object getBean(String beanID) {
        BeanDefinition bd = this.getBeanDefinition(beanID);
        if(bd == null){
            throw new BeanCreationException("Bean Definition does not exist");
        }
        if (bd.isSingleton()) {
            Object bean = this.getSingleton(beanID);
            if (bean == null) {
                bean = createBean(bd);
                this.registerSingleton(beanID, bean);
            }
            return bean;
        }
        return createBean(bd);
    }

    private Object createBean(BeanDefinition bd) {
        //创建实例
        Object bean = instantiateBean(bd);
        //设置属性
//        populateBean(bd, bean);
        populateBeanCommonsUtil(bd, bean);

        return bean;
    }

    /**
     * 大致流程如下：
     *  首先获取所有的属性PropertyValue，然后利用java8自带的JavaBean类库，获取这个类的相关属性，方法等参数，
     *  最后根据类中的属性名来判断是否是一样的，是一样的调用此属性的set方法注入。
     * @param bd
     * @param bean
     */
    private void populateBean(BeanDefinition bd, Object bean) {
        List<PropertyValue> pvs = bd.getPropertyValues();

        if (pvs == null || pvs.isEmpty()) {
            return;
        }

        BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this);
        SimpleTypeConverter converter = new SimpleTypeConverter();

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();

            for (PropertyValue pv : pvs) {
                String propertyName = pv.getName();
                Object originalValue = pv.getValue();
                Object resolvedValue = valueResolver.resolveValueIfNecessary(originalValue);

                for (PropertyDescriptor pd : pds) {
                    if (pd.getName().equals(propertyName)) {
                        Object convertedValue = converter.convertIfNecessary(resolvedValue, pd.getPropertyType());
                        //注意此处使用方法，在此类中都必须满足JavaBena的格式，否则方法会找不到
                        pd.getWriteMethod().invoke(bean, convertedValue);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            throw new BeanCreationException("Failed to obtain BeanInfo for class [" + bd.getBeanClassName() + "]", ex);
        }
    }

    /**
     * 采用Commons中的BeanUtil工具注入参数
     * @param bd
     * @param bean
     */
    private void populateBeanCommonsUtil(BeanDefinition bd, Object bean) {
        List<PropertyValue> pvs = bd.getPropertyValues();

        if (pvs == null || pvs.isEmpty()) {
            return;
        }

        BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this);

        try {
            for (PropertyValue pv : pvs) {
                String propertyName = pv.getName();
                Object originalValue = pv.getValue();
                Object resolvedValue = valueResolver.resolveValueIfNecessary(originalValue);
                BeanUtils.setProperty(bean, propertyName, resolvedValue);
            }
        } catch (Exception ex) {
            throw new BeanCreationException("Failed to obtain BeanInfo for class [" + bd.getBeanClassName() + "]", ex);
        }
    }

    private Object instantiateBean(BeanDefinition bd) {
        ClassLoader cl = this.getBeanClassLoader();
        String beanClassName = bd.getBeanClassName();
        try {
            Class<?> clz = cl.loadClass(beanClassName);
            return clz.newInstance();
        } catch (Exception e) {
            throw new BeanCreationException("Create bean for " + beanClassName + " fail");
        }
    }

    @Override
    public void setBeanClassLoader(ClassLoader beanClassLoader) {
        this.beanClassLoader = beanClassLoader;
    }

    @Override
    public ClassLoader getBeanClassLoader() {
        return (this.beanClassLoader != null ? this.beanClassLoader : ClassUtils.getDefaultClassLoader());
    }
}