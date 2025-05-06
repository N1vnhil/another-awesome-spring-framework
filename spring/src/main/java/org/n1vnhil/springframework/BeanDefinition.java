package org.n1vnhil.springframework;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class BeanDefinition {

    private String name;

    private Constructor<?> constructor;

    private Method postContructMethod;

    private List<Field> autowiredFields;

    private Class<?> beanType;

    public BeanDefinition(Class<?> clazz) {
        Component component = clazz.getDeclaredAnnotation(Component.class);
        this.name = component.name().isEmpty() ? clazz.getSimpleName() : component.name();
        this.beanType = clazz;
        try {
            this.constructor = clazz.getConstructor();
            this.postContructMethod = Arrays.stream(clazz.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(PostConstruct.class)).findFirst().orElse(null);
            autowiredFields = Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Autowired.class)).toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public Method getPostConstructMethod() {
        return postContructMethod;
    }

    public List<Field> getAutowiredFields() {
        return autowiredFields;
    }

    public Class<?> getBeanType() {
        return beanType;
    }

}
