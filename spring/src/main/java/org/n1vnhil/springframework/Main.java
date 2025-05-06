package org.n1vnhil.springframework;

import org.n1vnhil.springframework.sub.Dog;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException {
        ApplicationContext ioc = new ApplicationContext("org.n1vnhil.springframework");
        Object cat = ioc.getBean("Cat");
        Object dog = ioc.getBean(Dog.class);
        System.out.println(cat);
        System.out.println(dog);
    }

}
