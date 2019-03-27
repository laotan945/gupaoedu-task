package com.gupaoedu.servlet;

import com.gupaoedu.annotation.*;
import com.gupaoedu.mapping.HandlerMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author tzw
 * @Description: 启动加载类， 重写 init 、 doGet 、doPost 方法
 * @date 2019-03-25 17:24
 */
public class MyDispatcherServlet extends HttpServlet {


    /**
     * 保存 application.properties 配置文件的内容
     */
    private Properties properties = new Properties();

    /**
     * 对应对象实例 ioc 容器
     */
    private static Map<String, Object> ioc = new HashMap<>();


    /**
     * 保存 controller 所有方法请求关系容器
     */
    private static List<HandlerMapping> handlerMappings = new ArrayList<>();


    /**
     * 指定包下扫描到的类名 集合
     */
    private static List<String> classNames = new ArrayList<>();


    /**
     * 初始化加载配置
     */
    @Override
    public void init(ServletConfig config) {
        // TODO 1、 加载配置文件
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        doLoadConfig(contextConfigLocation);

        // TODO 2、扫描相关类
        String scanPackage = properties.getProperty("scanPackage");
        doScanner(scanPackage);


        // TODO 3、初始化扫描的类，并且将它们放入到ICO 容器之中
        doInstance();

        // TODO 4、完成依赖注入
        doAutowired();

        // TODO 5、初始化所有HandleMapping
        doHandlerMapping();


        System.out.println("Spring mvc mini init end");
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatcher(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception : " + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }


    /**
     * 直接从类路径下 找到Spring 主配置文件路径
     * 并且将其读取出来放到  properties 里面
     * 从文件中读取 scanPackage 放到内存中
     *
     * @param configPath 配置文件路径
     */
    private void doLoadConfig(String configPath) {
        InputStream fis = this.getClass().getClassLoader().getResourceAsStream(configPath);
        try {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 把包路径替换问文件路径,获取到 classpath
     */
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                // 递归
                String recursive = scanPackage.concat(".").concat(file.getName());
                doScanner(recursive);
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                } else {
                    String className = scanPackage.concat(".").concat(file.getName().replace(".class", ""));
                    classNames.add(className);
                }
            }
        }
    }


    /**
     * 初始化实例对象，为 DI 依赖注入（Dependecy Injection）做准备
     * 头部加了注解的类才初始化 如 action 、service
     * 如果类名是小写的，就不需要转换了
     */
    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        try {
            for (String className : classNames) {

                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyController.class)) {
                    Object value = clazz.newInstance();
                    String key = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(key, value);


                } else if (clazz.isAnnotationPresent(MyService.class)) {
                    // 1、默认类名首字母小写
                    MyService service = clazz.getAnnotation(MyService.class);

                    // 2、自定义 beanName
                    String beanName = service.value();
                    if ("".equals(beanName)) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }
                    ioc.put(beanName, clazz.newInstance());

                    // 3、根据类型自动赋值 获取类下面的所有接口类 ？？？？？
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The " + i.getName() + "is exists !!!");
                        }
                        ioc.put(i.getName(), clazz.newInstance());
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 首字母转小写
     *
     * @param beanName
     * @return
     */
    private String toLowerFirstCase(String beanName) {
        char[] arr = beanName.toCharArray();
        arr[0] += 32;
        return String.valueOf(arr);
    }


    /**
     * 自动注入对象
     */
    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            // 获取对象 声明的所有字段类表
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {

                if (!field.isAnnotationPresent(MyAutowired.class)) {
                    continue;
                }

                MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                // 拿到属性值作为 ioc中的 key 获取到对应值
                String beanName = autowired.value().trim();

                // 如果用户没有自定义 beanName, 默认就是根据类型注入
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }

                // 非public 修饰的的内容需要设置暴力访问
                field.setAccessible(true);
                try {
                    // 复制对象到注入对象属性中
                    field.set(entry.getValue(), ioc.get(beanName));

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }


//    /**
//     * 判断搜字母是否为小写
//     *
//     * @param beanName
//     * @return
//     */
//    private String isAcronym(String beanName) {
//        char[] arr = beanName.toCharArray();
//        // 小写字符的数值是 'a'=97到 'z'=122
//        // 大写字符的数值是 'A'=65到 ''Z=90
//        if (arr[0] >= 97 && arr[0] <= 122) {
//            return beanName;
//        }
//        return toLowerFirstCase(beanName);
//    }


    /**
     * 初始化 controller 下的 所有  Method
     */
    private void doHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(MyController.class)) {
                continue;
            }

            // Controller 上的请求地址
            String baseUrl = "";
            if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping requestMapper = clazz.getAnnotation(MyRequestMapping.class);
                baseUrl = requestMapper.value();
            }

            // 所有Controller中的带有 @MyRequestMapping注解 方法封装缓存到 handlerMappings 中
            for (Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(MyRequestMapping.class)) {
                    MyRequestMapping requestMapper = method.getAnnotation(MyRequestMapping.class);

                    String regex = ("/" + baseUrl + requestMapper.value()).replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);
                    handlerMappings.add(new HandlerMapping(pattern,entry.getValue(),method));
                }
            }
        }
    }


    /**
     * Get、 Post 调用
     * @param request
     * @param response
     */
    private void doDispatcher(HttpServletRequest request, HttpServletResponse response) throws Exception {


        HandlerMapping handlerMapping = HandlerMapping.getHandlerMapping(request,handlerMappings);
        if(handlerMapping == null){
            response.getWriter().write("404 Not Found!!");
            return;
        }

        // TODO 从request中获取url中的参数名和参数值  例如： key = "name" value = "输入姓名"
        Map<String, String[]> params = request.getParameterMap();

        // TODO 参数类型和值类型集合
        Class<?>[] paramTypes = handlerMapping.getMethod().getParameterTypes();
        Object[]  paramValues = new Object[paramTypes.length];
        Map<String, Integer> paramIndexMapping = handlerMapping.getParamIndexMapping();


        // 如果找到匹配的对象，则开始填充参数值
        params.entrySet().forEach(param->{
            if(paramIndexMapping.containsKey(param.getKey())){
                int index = paramIndexMapping.get(param.getKey());
                paramValues[index] = convert(paramTypes[index],param.getValue());
            }
        });

        // 设置方法中的request和response对象 ,如果方法请求中有参数名 叫 request、response呢 ？？
        int reqIndex = paramIndexMapping.get(HttpServletRequest.class.getName());
        paramValues[reqIndex] = request;
        int respIndex = paramIndexMapping.get(HttpServletResponse.class.getName());
        paramValues[respIndex] = response;
        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(), paramValues);

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf-8");
        // 打印返回到调用页
        if(result == null || result instanceof Void){
            response.getWriter().write("没有返回值");
            return ;
        }
        response.getWriter().write(result.toString());
    }






    /**
     * 准换数据类型
     * @param type
     * @param values
     * @return
     */
    private Object convert(Class<?> type, String[] values){
        String value = Arrays.toString(values).replaceAll("\\[|\\]", "").replaceAll("\\s", ",");
        if(type == String.class){
           return String.valueOf(value);
        }else if(type == Integer.class || type == int.class){
            return Integer.valueOf(value);
        }else if(type == Double.class  || type == double.class){
            return Double.valueOf(value);
        }else if(type == Boolean.class || type == boolean.class){
            return Boolean.valueOf(value);
        }else if(type == Byte.class    || type == byte.class){
            return Byte.valueOf(value);
        }else if(type == Long.class    || type == long.class){
            return Long.valueOf(value);
        }else if(type == Float.class   || type == float.class){
            return Float.valueOf(value);
        }else if(type == Short.class   || type == short.class){
            return Short.valueOf(value);
        }
        return value;
    }


}
