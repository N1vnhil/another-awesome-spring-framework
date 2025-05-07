package org.n1vnhil.springframework.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.n1vnhil.springframework.BeanPostProcessor;
import org.n1vnhil.springframework.Component;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DispatchServlet extends HttpServlet implements BeanPostProcessor {

    Map<String, WebHandler> handlerMap = new HashMap<>();

    private static final Pattern PATTERN = Pattern.compile("xxx\\{(.*?)}");

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
             Object[] args = resolveArgs(req, webHandler.getMethod());
             Object result = webHandler.getMethod().invoke(controllerBean, args);
             switch (webHandler.getResultType()) {
                 case JSON -> {
                     resp.setContentType("application/json;charset=UTF-8");
                     resp.getWriter().write(JSONObject.toJSONString(result));
                 }
                 case HTML -> {
                     resp.setContentType("text/html;charset=UTF-8");
                     resp.getWriter().write(result.toString());
                 }
                 case LOCAL -> {
                     ModelAndView mv = (ModelAndView) result;
                     String view = mv.getView();
                     InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(view);
                     try(resourceAsStream) {
                         String html = new String(resourceAsStream.readAllBytes());
                         html = renderTemplate(html, mv.getContext());
                         resp.setContentType("text/html;charset=UTF-8");
                         resp.getWriter().write(html);
                     }
                 }
             }
         } catch (Exception e) {
             throw new ServletException(e);
         }
    }

    private String renderTemplate(String html, Map<String, String> context) {
        Matcher matcher = PATTERN.matcher(html);
        StringBuilder sb = new StringBuilder();
        while(matcher.find()) {
            String key = matcher.group(1);
            String value = context.getOrDefault(key, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private Object[] resolveArgs(HttpServletRequest req, Method method) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for(int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String value = null;
            Param param = parameter.getAnnotation(Param.class);
            if(param != null) value = req.getParameter(param.value());
            else value = req.getParameter(parameter.getName());

            Class<?> parameterType = parameter.getType();
            if(String.class.isAssignableFrom(parameterType)) {
                args[i] = value;
            } else if(Integer.class.isAssignableFrom(parameterType)) {
                args[i] = Integer.parseInt(value);
            } else {
                args[i] = null;
            }
        }
        return args;
    }

    private WebHandler findHandler(HttpServletRequest req) {
        return handlerMap.get(req.getRequestURI());
    }

    @Override
    public Object afterInitializeBean(Object bean, String beanName) {
        if(!bean.getClass().isAnnotationPresent(Controller.class)) return bean;
        RequestMapping classRm = bean.getClass().getAnnotation(RequestMapping.class);
        String classUrl = classRm != null ? classRm.value() : "";
        Arrays.stream(bean.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(RequestMapping.class))
                .forEach(m -> {
                    RequestMapping methodRm = m.getAnnotation(RequestMapping.class);
                    String key = classUrl.concat(methodRm.value());
                    WebHandler webHandler= new WebHandler(bean, m);
                    if(handlerMap.put(key, webHandler) != null) throw new RuntimeException("Controller定义重复");
                });

        return bean;
    }

}
