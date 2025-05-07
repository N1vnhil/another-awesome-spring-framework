package org.n1vnhil.springframework.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.n1vnhil.springframework.Autowired;
import org.n1vnhil.springframework.Component;
import org.n1vnhil.springframework.PostConstruct;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.IOException;
import java.util.logging.LogManager;

@Component
public class TomcatServer {

    @Autowired
    private DispatchServlet dispatchServlet;

    @PostConstruct
    public void start() throws LifecycleException {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        int port = 8080;
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();

        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext(contextPath, docBase);

        tomcat.addServlet(context, "demoServlet", dispatchServlet);

        context.addServletMappingDecoded("/*", "demoServlet");
        tomcat.start();
        System.out.println("tomcat start... port: " + port);
    }

}
