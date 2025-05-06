package org.n1vnhil.springframework;

import java.util.List;

public class ApplicationContext {

    public ApplicationContext(String packageName) {
        initContext(packageName);
    }

    public Object getBean(String name) {
        return null;
    }

    public <T> T getBean(Class<T> beanType) {
        return null;
    }

    public <T> List<T> getBeans(Class<T> beanType) {
        return null;
    }

    public void initContext(String packageName) {
        scanPackage(packageName).stream().filter(this::scanCreate).map(this::wrapper).forEach(this::createBean);
    }

    protected  BeanDefinition wrapper(Class<?> clazz) {
        return new BeanDefinition(clazz);
    }

    protected void createBean(BeanDefinition beanDefinition) {

    }

    protected boolean scanCreate(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class);
    }

    private List<Class<?>> scanPackage(String packageName) {
        return null;
    }

}
