package com.gupaoedu.mapping;

import com.gupaoedu.annotation.MyRequestMapping;
import com.gupaoedu.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author tzw
 * @Description:  [策略模式] 保存了一个 url  和一个 method 关系
 * @date 2019-03-26 19:57
 */
public class HandlerMapping {

    /**
     * 请求地址
     */
    private Pattern pattern ;

    /**
     * 控制器内的方法相关信息
     */
    private Method method;

    /**
     * 控制器 实例对象
     */
    private Object controller;

    /**
     * 请求方法参数类型集合
     */
    private Class<?>[] paramTypes;

    /**
     * 形参列表  参数名字为 key ,参数的顺序 作为值
     */
    private Map<String, Integer> paramIndexMapping;


    public HandlerMapping(Pattern pattern, Object controller , Method method) {
        this.pattern = pattern;
        this.method = method;
        this.controller = controller;
        this.paramTypes = method.getParameterTypes();
        this.paramIndexMapping = new HashMap<>();
        putParamIndexMapping (method);
    }


    /**
     * 应为一个字段可以有多个注解，而一个方法又有多个参数
     * @param method
     */
    private void putParamIndexMapping(Method method) {

        // 提取方法中加了注解的参数
        Annotation[] [] pa = method.getParameterAnnotations();
        for (int i = 0; i < pa.length ; i ++) {
            for(Annotation a : pa[i]){
                if(a instanceof MyRequestParam){
                    String paramName = ((MyRequestParam) a).value();
                    if(!"".equals(paramName.trim())){
                        paramIndexMapping.put(paramName, i);
                    }
                }
            }
        }

        //提取方法中的request和response参数
        Class<?> [] paramsTypes = method.getParameterTypes();
        for (int i = 0; i < paramsTypes.length ; i ++) {
            Class<?> type = paramsTypes[i];
            if(type == HttpServletRequest.class ||
                    type == HttpServletResponse.class){
                paramIndexMapping.put(type.getName(),i);
            }
        }
    }


    /**
     * 根据请求地址，获取到对应 handlerMapping 对象信息
     * @param req
     * @return
     * @throws Exception
     */
    public static HandlerMapping getHandlerMapping(HttpServletRequest req, List<HandlerMapping> handlerMappings)throws Exception{
        if(handlerMappings.isEmpty()){ return null; }

        String url = req.getRequestURI();
        String contextPath = req.getContextPath().trim();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");
        for (HandlerMapping handler : handlerMappings) {
            try{
                if(handler.getPattern().matcher(url).matches()){
                    return handler;
                }
            }catch(Exception e){
                throw e;
            }
        }
        return null;
    }


    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Map<String, Integer> getParamIndexMapping() {
        return paramIndexMapping;
    }

    public void setParamIndexMapping(Map<String, Integer> paramIndexMapping) {
        this.paramIndexMapping = paramIndexMapping;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }
}
