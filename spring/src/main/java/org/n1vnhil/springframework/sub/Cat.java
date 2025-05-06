package org.n1vnhil.springframework.sub;

import org.n1vnhil.springframework.Autowired;
import org.n1vnhil.springframework.Component;
import org.n1vnhil.springframework.PostConstruct;

@Component
public class Cat {

    @Autowired
    private Dog dog;

    @PostConstruct
    public void init() {
        System.out.println("Init Cat.");
    }
}
