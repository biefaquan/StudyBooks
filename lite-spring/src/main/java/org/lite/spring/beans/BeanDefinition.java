package org.lite.spring.beans;

/**
 * Created by bfq on 2020/2/13
 */
public interface BeanDefinition {
    public static final String SCOPE_SINGLETON = "single";
    public static final String SCOPE_PROTOTYPE = "prototype";
    public static final String SCOPE_DEFAULT = "";

    public String getBeanClassName();

    boolean isSingleton();

    boolean isPrototype();

    String getScope();
}
