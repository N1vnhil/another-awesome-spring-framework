package org.n1vnhil.springframework.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.n1vnhil.springframework.BeanPostProcessor;
import org.n1vnhil.springframework.Component;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class DispatchServlet extends HttpServlet implements BeanPostProcessor {

    Map<String, WebHandler> handlerMap = new HashMap<>();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         WebHandler webHandler = findHandler(req);
         if(Objects.isNull(webHandler)) {
             resp.setContentType("text/html");
             resp.getWriter().write("<h1>Error, no corresponding handler to your request!</h1><br>");
             return;
         }

         try {
             Object controllerBean = webHandler.getControllerBean();
             Object result = webHandler.getMethod().invoke(controllerBean);
             switch (webHandler.getResultType()) {
                 case JSON -> {
                     resp.setContentType("application/json;charset=UTF-8");
                     resp.getWriter().write(JSONObject.toJSONString(result));
                 }
                 case HTML -> {
                     resp.setContentType("application/json;charset=UTF-8");
                     resp.getWriter().write(result.toString());
                 }
                 case LOCAL -> {}
             }
         } catch (Exception e) {
             throw new ServletException(e);
         }
    }

    private WebHandler findHandler(HttpServletRequest req) {
        return handlerMap.get(req.getRequestURI());
    }

    @Override
    public Object afterInitializeBean(Object bean, String beanName) {
        if(!bean.getClass().isAnnotationPresent(Controller.class)) return bean;
        String classUrl = "";
        RequestMapping classRm = bean.getClass().getAnnotation(RequestMapping.class);
        if(Objects.nonNull(classRm)) {
            classUrl = classRm.value();
        }



        return bean;
    }

}
