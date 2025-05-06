package org.n1vnhil.springframework;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException {
        ApplicationContext ioc = new ApplicationContext("org.n1vnhil.springframework");
        Object cat = ioc.getBean("Cat");
        System.out.println(cat);
    }

}
