package org.n1vnhil.springframework;

import java.beans.MethodDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class ApplicationContext {

    public ApplicationContext(String packageName) throws IOException, URISyntaxException {
        initContext(packageName);
    }

    private Map<String, Object> ioc = new HashMap<>();

    private Map<String, Object> loadingIoc = new HashMap<>();

    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    private List<BeanPostProcessor> postProcessors = new ArrayList<>();

    public Object getBean(String name) {
        if(Objects.isNull(name) || name.isEmpty()) return null;
        Object bean = this.ioc.get(name);
        if(Objects.nonNull(bean)) return bean;
        if(beanDefinitionMap.containsKey(name)) return createBean(beanDefinitionMap.get(name));
        return null;
    }

    public <T> T getBean(Class<T> beanType) {
        String beanName = this.beanDefinitionMap.values().stream()
                .filter(bd -> beanType.isAssignableFrom(bd.getBeanType()))
                .map(BeanDefinition::getName)
                .findFirst()
                .orElse(null);
        return (T) getBean(beanName);
    }

    public <T> List<T> getBeans(Class<T> beanType) {
        return this.beanDefinitionMap.values().stream()
                .filter(bd -> beanType.isAssignableFrom(bd.getBeanType()))
                .map(BeanDefinition::getName)
                .map(this::getBean)
                .map(m -> (T) m)
                .toList();
    }

    public void initContext(String packageName) throws IOException, URISyntaxException {
        scanPackage(packageName).stream().filter(this::scanCreate).forEach(this::wrapper);
        initBeanPostProcessor();
        beanDefinitionMap.values().forEach(this::createBean);
    }

    private void initBeanPostProcessor() {
        beanDefinitionMap.values().stream()
                .filter(bd -> BeanPostProcessor.class.isAssignableFrom(bd.getBeanType()))
                .map(this::createBean)
                .map(bean -> (BeanPostProcessor) bean)
                .forEach(postProcessors::add);
    }

    protected  BeanDefinition wrapper(Class<?> clazz) {
        BeanDefinition beanDefinition = new BeanDefinition(clazz);
        if(beanDefinitionMap.containsKey(beanDefinition.getName())) {
            throw new RuntimeException("Bean名称重复");
        }
        beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
        return beanDefinition;
    }

    protected Object createBean(BeanDefinition beanDefinition) {
        String name = beanDefinition.getName();
        if(ioc.containsKey(name)) return ioc.get(name);
        if(loadingIoc.containsKey(name)) return loadingIoc.get(name);
        return doCreateBean(beanDefinition);
    }

    private Object doCreateBean(BeanDefinition beanDefinition) {
        Constructor<?> constructor = beanDefinition.getConstructor();
        Object bean = null;
        try {
            bean = constructor.newInstance();
            loadingIoc.put(beanDefinition.getName(), bean);
            autowiredBean(bean, beanDefinition);
            bean = initializeBean(bean, beanDefinition);
            loadingIoc.remove(beanDefinition.getName());
            ioc.put(beanDefinition.getName(), bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bean;
    }

    protected boolean scanCreate(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class);
    }

    private List<Class<?>> scanPackage(String packageName) throws IOException, URISyntaxException {
        List<Class<?>> classList = new ArrayList<>();
        URL resource = this.getClass().getClassLoader().getResource(packageName.replace(".", "\\"));
        Path path = Paths.get(resource.toURI());
        Files.walkFileTree(path, new SimpleFileVisitor<>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                Path absolutePath = file.toAbsolutePath();
                if(absolutePath.toString().endsWith(".class")) {
                    String replaceStr = absolutePath.toString().replace("\\", ".");
                    int packageIdx = replaceStr.indexOf(packageName);
                    String className = replaceStr.substring(packageIdx, replaceStr.length() - ".class".length());
                    try {
                         classList.add(Class.forName(className));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return classList;
    }

    private void autowiredBean(Object bean, BeanDefinition beanDefinition) throws IllegalAccessException {
        for(Field autowiredField: beanDefinition.getAutowiredFields()) {
            autowiredField.setAccessible(true);
            autowiredField.set(bean, getBean(autowiredField.getType()));
        }
    }

    private Object initializeBean(Object bean, BeanDefinition beanDefinition) throws InvocationTargetException, IllegalAccessException {
        for(BeanPostProcessor postProcessor: postProcessors) {
            bean = postProcessor.beforeInitializeBean(bean, beanDefinition.getName());
        }

        Method postConstructMethod = beanDefinition.getPostConstructMethod();
        if(Objects.nonNull(postConstructMethod)) postConstructMethod.invoke(bean);

        for(BeanPostProcessor postProcessor: postProcessors) {
            bean = postProcessor.afterInitializeBean(bean, beanDefinition.getName());
        }

        return bean;
    }
}
