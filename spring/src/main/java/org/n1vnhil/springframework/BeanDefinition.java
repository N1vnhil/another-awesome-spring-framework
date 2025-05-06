package org.n1vnhil.springframework;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

public class BeanDefinition {

    private String name;

    private Constructor<?> constructor;

    private Method postContructMethod;

    public BeanDefinition(Class<?> clazz) {
        Component component = clazz.getDeclaredAnnotation(Component.class);
        this.name = component.name().isEmpty() ? clazz.getSimpleName() : component.name();
        try {
            this.constructor = clazz.getConstructor();
            this.postContructMethod = Arrays.stream(clazz.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(PostConstruct.class)).findFirst().orElse(null);
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

}
