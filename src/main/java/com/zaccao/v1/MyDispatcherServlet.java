package com.zaccao.v1;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MyDispatcherServlet extends HttpServlet {

    private Properties contextConfig = new Properties();
    // cache the full class name scanned from the pacakage
    private List<String> classNames = new ArrayList<>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatch();
    }

    private void doDispatch() {
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1 
        doLoadConfig();
        //2 
        doScan();

        doInitInstance();
        doAutowired();
        doInitHandlerMapping();

    }

    private void doInitHandlerMapping() {
    }

    private void doAutowired() {
    }

    private void doInitInstance() {
    }

    private void doScan(String scanPackage) {
        // convert class path to directory and iterate through the class files
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()
        ) {
            // when there's nested folders , go deeper
            if (file.isDirectory()) {
                // append the child folder name to root path and recursively calls the method itself
                doScan(scanPackage + "." + file.getName());
            } else {
                // if it's a file of another format , just skip it
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                // convert the class file to class name
                String className = scanPackage + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    // find config file in class path  designated by 'contextConfigLocation' (java's servlet config mechanism)
    private void doLoadConfig(String contextConfigLocation) {

        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation)) {
            contextConfig.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
