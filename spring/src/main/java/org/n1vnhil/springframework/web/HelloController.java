package org.n1vnhil.springframework.web;

@Controller
@RequestMapping("/hello")
public class HelloController {

    @RequestMapping("/test")
    public String test() {
        return "hello";
    }

}
