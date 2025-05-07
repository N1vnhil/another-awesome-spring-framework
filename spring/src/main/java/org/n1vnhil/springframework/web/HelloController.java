package org.n1vnhil.springframework.web;

import ch.qos.logback.core.model.Model;
import org.apache.tomcat.util.log.UserDataHelper;
import org.n1vnhil.springframework.Component;

@Controller
@Component
@RequestMapping("/hello")
public class HelloController {

    @RequestMapping("/test")
    public String test(@Param("name") String name, @Param("age") Integer age) {
        return String.format("<h1>hello</h1><br> name:%s   age:%s", name, age);
    }

    @ResponseBody
    @RequestMapping("/json")
    public User json(@Param("name") String name, @Param("age") Integer age) {
        User user = new User();
        user.setAge(age);
        user.setName(name);
        return user;
    }

    @RequestMapping("/html")
    public ModelAndView html(@Param("name") String name, @Param("age") Integer age) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView("index.html");
        modelAndView.getContext().put("name", name);
        modelAndView.getContext().put("age", age.toString());
        return modelAndView;
    }

}
