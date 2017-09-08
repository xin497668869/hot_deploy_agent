package com.xin;

import com.alibaba.fastjson.JSON;
import com.xin.vo.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.xin.BootClass.dbDetailInfoVo;
import static com.xin.BootClass.sqlList;
import static com.xin.BootClass.stackTrace;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description
 */
@Component
public class Test implements ApplicationContextAware {

    @Autowired
    private ApplicationContext applicationContext;

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @PostConstruct
    public void startServer() {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(BootClass.port);
                while (true) {
                    Socket socket = serverSocket.accept();
                    ResponseData responseData;
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    System.out.println("准备拿东西");
                    String content = br.readLine();
                    System.out.println("拿完了吗? " + content);
                    String[] split = content.split(":::");
                    System.out.println("助理中");
                    responseData = handle(split[0], split[1]);
                    System.out.println("助理完了");
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    System.out.println("准备写入啦!!!!!!!!!!!!!!!");
                    bw.write(JSON.toJSONString(responseData) + "\r\n");
                    bw.flush();
                    System.out.println("!!!" + JSON.toJSONString(responseData) + "!!!");
                    System.out.println(JSON.toJSONString(responseData) + "\r\n");
                    System.out.println("写完啦");

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public ResponseData handle(String beanClassName, String methodName) {

        int time = 1;
        while (time != 0) {
            time--;

            try {
                Class<?> aClass = Class.forName(beanClassName);
                Object bean = applicationContext.getBean(aClass);
                if (bean == null) {
                    System.err.println("===== 这个类还没被注入 可以使用@Component 注解类");
                }
                Method method = bean.getClass().getDeclaredMethod(methodName);
                method.setAccessible(true);
                String uuid = UUID.randomUUID().toString().replace("-", "");
                System.out.println("===== 热测试启动 [" + dateFormat.format(new Date()) + "] [" + uuid + "] 方法:" + methodName + " 类:" + beanClassName + " =====");
                long s = System.currentTimeMillis();
                long interval = 0;
                try {
                    Object invoke = method.invoke(bean);
                    interval = System.currentTimeMillis() - s;
                    System.out.println("===== 返回值是: \n" + JSON.toJSONString(invoke, true));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("===== 热测试结束 [" + dateFormat.format(new Date()) + "] [" + uuid + "] 时间为:" + getTime(interval) + "ms  方法:" + methodName + " 类:" + beanClassName + " =====");
                System.out.println();
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                System.out.println("===== compile完成再次请再次运行");
            } catch (Throwable e) {
                e.printStackTrace();

            } finally {
                ResponseData responseData = new ResponseData();
                responseData.setDbDetailInfoVo(dbDetailInfoVo);
                List<String> sql = sqlList.get();
                if (sql == null) {
                    sql = Collections.emptyList();
                }
                responseData.setSql(sql);
                sqlList.remove();

                StackTraceElement[] stackTraceElements = stackTrace.get();
                if (stackTraceElements != null) {
                    for (StackTraceElement stackTraceElement : stackTraceElements) {
                        System.out.println(stackTraceElement);
                    }
                    System.out.println("!!!!!!!!!!!!!!!!!!!");
                    Predicate<StackTraceElement> stackTraceElementPredicate = stackTraceElement ->
                            !stackTraceElement.getClassName().startsWith("java.")
                                    && !stackTraceElement.getClassName().startsWith("sun.")
                                    && !stackTraceElement.getClassName().startsWith("org.hibernate")
                                    && !stackTraceElement.getClassName().startsWith("com.xin")
                                    && !stackTraceElement.getClassName().startsWith("com.mysql")
                                    && !stackTraceElement.getClassName().startsWith("com.alibaba")
                                    && !stackTraceElement.getClassName().startsWith("org.springframework");
                    List<StackTraceElement> traceElementsList = Arrays.stream(stackTraceElements).filter(stackTraceElementPredicate).collect(Collectors.toList());
                    responseData.setStackTraceElement(traceElementsList.toArray(new StackTraceElement[traceElementsList.size()]));
                    for (StackTraceElement stackTraceElement : traceElementsList) {
                        System.out.println(stackTraceElement);
                    }
                    System.out.println("==============");
                    stackTrace.remove();
                }
                return responseData;
            }
        }
        return new ResponseData();
    }


    public static String getTime(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        } else {
            String misecondStr = String.valueOf(millis);
            return misecondStr.substring(0, misecondStr.length() - 3) + "," + misecondStr.substring(misecondStr.length() - 3);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}