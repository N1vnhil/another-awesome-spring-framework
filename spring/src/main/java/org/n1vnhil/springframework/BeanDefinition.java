package org.n1vnhil.springframework;

import java.lang.reflect.Constructor;

public class BeanDefinition {

    private String name;

    private Constructor<?> constructor;

    public BeanDefinition(Class<?> clazz) {
        Component component = clazz.getDeclaredAnnotation(Component.class);
        this.name = component.name().isEmpty() ? clazz.getSimpleName() : component.name();
        try {
            this.constructor = clazz.getConstructor();
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

}
