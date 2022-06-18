package com.zaccao.v1;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

public class MyDispatcherServlet extends HttpServlet {

    private Properties contextConfig = new Properties();
    // cache the full class name scanned from the pacakage
    private List<String> classNames = new ArrayList<>();

   
    private Map<String,Object> IOCContainer = new HashMap<String,Object>();

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
        if(IOCContainer.isEmpty()){ return; }

        for (Map.Entry<String, Object> entry : IOCContainer.entrySet()) {
            for (Field field : entry.getValue().getClass().getDeclaredFields()) {
                if(!field.isAnnotationPresent(MyAutowired.class)){ continue; }

                MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                String beanName = autowired.value().trim();
                if("".equals(beanName)){
                    beanName = field.getType().getName();
                }


                field.setAccessible(true);

                try {
                    field.set(entry.getValue(),IOCContainer.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    private void doInitInstance() {

            if(classNames.isEmpty()){ return; }

            try {
                for (String className : classNames) {
                    Class<?> clazz = Class.forName(className);

                    if(clazz.isAnnotationPresent(MyController.class)) {
                        String beanName = firstLetterToLowerCase(clazz.getSimpleName());
                        Object instance = clazz.newInstance();
                        IOCContainer.put(beanName, instance);
                    }else if(clazz.isAnnotationPresent(MyService.class)){

                        //1、默认类名首字母小写
                        String beanName = firstLetterToLowerCase(clazz.getSimpleName());

                        //2、如果在多个包下出现了相同的类名，优先是用别名（自定义命名）
                        MyService service = clazz.getAnnotation(MyService.class);
                        if(!"".equals(service.value())){
                            beanName = service.value();
                        }
                        Object instance = clazz.newInstance();
                        IOCContainer.put(beanName, instance);

                        //3、如果是接口,只能初始化的它的实现类
                        for (Class<?> i : clazz.getInterfaces()) {
                            if(IOCContainer.containsKey(i.getName())){
                                throw new Exception("The " + i.getName() + " is exists,please use alies!!");
                            }
                            IOCContainer.put(i.getName(),instance);
                        }

                    }else {
                        continue;
                    }

                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    private String firstLetterToLowerCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;     //利用了ASCII码，大写字母和小写相差32这个规律
        return String.valueOf(chars);
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
