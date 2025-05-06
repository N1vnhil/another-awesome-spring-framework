package org.n1vnhil.springframework;

import java.beans.MethodDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
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

    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    public Object getBean(String name) {
        return this.ioc.get(name);
    }

    public <T> T getBean(Class<T> beanType) {
        return this.ioc.values().stream().filter(bean -> beanType.isAssignableFrom(bean.getClass()))
                .map(bean -> (T) bean).findAny().orElse(null);
    }

    public <T> List<T> getBeans(Class<T> beanType) {
        return this.ioc.values().stream().filter(bean -> beanType.isAssignableFrom(bean.getClass()))
                .map(bean -> (T) bean).toList();
    }

    public void initContext(String packageName) throws IOException, URISyntaxException {
        scanPackage(packageName).stream().filter(this::scanCreate).map(this::wrapper).forEach(this::createBean);
    }

    protected  BeanDefinition wrapper(Class<?> clazz) {
        BeanDefinition beanDefinition = new BeanDefinition(clazz);
        if(beanDefinitionMap.containsKey(beanDefinition.getName())) {
            throw new RuntimeException("Bean名称重复");
        }
        beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
        return beanDefinition;
    }

    protected void createBean(BeanDefinition beanDefinition) {
        String name = beanDefinition.getName();
        if(ioc.containsKey(name)) return;
        doCreateBean(beanDefinition);
    }

    private void doCreateBean(BeanDefinition beanDefinition) {
        Constructor<?> constructor = beanDefinition.getConstructor();
        Object bean = null;
        try {
            bean = constructor.newInstance();
            Method postConstructMethod = beanDefinition.getPostConstructMethod();
            if(Objects.nonNull(postConstructMethod)) postConstructMethod.invoke(bean);
        } catch (Exception e) {
            throw new RuntimeException();
        }
        ioc.put(beanDefinition.getName(), bean);
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

}
