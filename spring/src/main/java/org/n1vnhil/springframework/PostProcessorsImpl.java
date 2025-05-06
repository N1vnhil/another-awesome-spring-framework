package org.n1vnhil.springframework;


@Component
public class PostProcessorsImpl implements BeanPostProcessor{

    @Override
    public Object beforeInitializeBean(Object bean, String beanName) {
        System.out.println("Init " + beanName);
        return bean;
    }

    @Override
    public Object afterInitializeBean(Object bean, String beanName) {
        System.out.println("Init " + beanName);
        return bean;
    }
}
