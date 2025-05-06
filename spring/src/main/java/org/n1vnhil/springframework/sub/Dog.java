package org.n1vnhil.springframework.sub;

import org.n1vnhil.springframework.Autowired;
import org.n1vnhil.springframework.Component;
import org.n1vnhil.springframework.PostConstruct;

@Component(name = "myDog")
public class Dog {

    @Autowired
    Cat cat;

    @PostConstruct
    public void init() {
        System.out.println("Init dog with filed " + cat);
    }

}
