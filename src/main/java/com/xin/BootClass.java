package com.xin;

import com.xin.change.BootstrapChangeClass;
import com.xin.change.ComponentScanAnnotationParserChangeClass;
import com.xin.change.MySqlChangeClass;
import com.xin.change.MySqlConfigChangeClass;
import com.xin.change.WebappLoaderChangeClass;
import com.xin.monitor.MethodAddMonitorClassAdapter;
import com.xin.vo.DbDetailInfoVo;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class BootClass {
    public static Instrumentation inst;
    public static Integer port = -1;
    public static DbDetailInfoVo dbDetailInfoVo;

    public static void premain(String agentArgs, Instrumentation instrumentation) throws Exception {
//        System.out.println("插件参数为:" + agentArgs);
        inst = instrumentation;
        try {
            port = Integer.valueOf(agentArgs);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("热部署插件参数 必须是整数" + " 当前: " + agentArgs);
        }
        if (port < 1) {
            throw new Exception("热部署插件参数 端口有问题 当前: " + port);
        }
        System.out.println("当前端口为:" + port);
        try {
            ClassLoader.getSystemClassLoader().loadClass("org.springframework.core.env.Environment");
            System.out.println("没有用tomcat启动");
            initClass(ClassLoader.getSystemClassLoader());
        } catch (ClassNotFoundException e) {
            System.out.println("用tomcat启动");
            bootstrapClassChange();
            e.printStackTrace();
        }
    }

    private static void bootstrapClassChange() throws ClassNotFoundException, UnmodifiableClassException, IOException {
        try {
            String className = "org.apache.catalina.startup.Bootstrap";
            byte[] bytes = new BootstrapChangeClass().writeFile(ClassLoader.getSystemClassLoader(), className);
            Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(className);
            ClassDefinition classDefinition = new ClassDefinition(aClass, bytes);
            inst.redefineClasses(classDefinition);
            System.out.println("Bootstrap 替换成功 " + aClass);
        } catch (ClassNotFoundException e) {
            System.out.println("tomcat 注入失败, 无法注入所有功能: " + e.getMessage());
        }

    }

    /**
     * 主要用途替换新增用途的
     *
     * @param classLoader
     * @throws ClassNotFoundException
     * @throws UnmodifiableClassException
     * @throws FileNotFoundException
     */
    public static void initClass(ClassLoader classLoader) throws ClassNotFoundException, UnmodifiableClassException, IOException {

        System.out.println("准备替换功能模块");
        try {
            mySqlChangeClassChange(classLoader);
        } catch (Exception e) {
            System.err.println("mysql 的PreparedStatement替换失败");
        }
        try {
            componentScanAnnotationParserChangeClass(classLoader);
        } catch (Exception e) {
            System.err.println("componentScanAnnotationParser替换失败");
        }

        try {
            mySqlConfigChangeClass(classLoader);
        } catch (Exception e) {
            System.err.println("ConnectionImpl替换失败");
        }

        try {
            adddMethodMonitor(classLoader);
        } catch (Exception e) {
            System.out.println("添加监控异常");
        }

    }

    private static void adddMethodMonitor(ClassLoader classLoader) throws IOException {
        String path = "com.gaoxiang.performance.injection.StatDataServiceImpl";
        //todo  获取指定范围内所有的文件路径
        ClassReader cr = new ClassReader(path);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new MethodAddMonitorClassAdapter.AddMonitorClassAdapter(cw);
        cr.accept(cv, Opcodes.ASM6);
    }

    private static void mySqlChangeClassChange(ClassLoader classLoader) throws ClassNotFoundException, UnmodifiableClassException, IOException {
        try {
            String className = "com.mysql.jdbc.PreparedStatement";
            Class<?> aClass = classLoader.loadClass(className);

            ClassDefinition classDefinition = new ClassDefinition(aClass, new MySqlChangeClass().writeFile(classLoader, className));
            inst.redefineClasses(classDefinition);
            System.out.println("可进行sql显示: PreparedStatement替换成功");
        } catch (ClassNotFoundException e) {
            System.out.println("sql显示类注入失败, 无法进行sql显示: " + e.getMessage());
        }

    }

    private static void mySqlConfigChangeClass(ClassLoader classLoader) throws UnmodifiableClassException, ClassNotFoundException, IOException {
        try {
            String className = "com.mysql.jdbc.ConnectionImpl";
            Class<?> aClass = classLoader.loadClass(className);

            ClassDefinition classDefinition = new ClassDefinition(aClass, new MySqlConfigChangeClass().writeFile(classLoader, className));
            inst.redefineClasses(classDefinition);
            System.out.println("获取数据库配置信息: ConnectionImpl替换成功");
        } catch (ClassNotFoundException e) {
            System.out.println("热部署类注入失败, 无法获取数据库配置信息: " + e.getMessage());
        }
    }

    private static void componentScanAnnotationParserChangeClass(ClassLoader classLoader) throws UnmodifiableClassException, IOException {
        try {
            String className = "org.springframework.context.annotation.ComponentScanAnnotationParser";
            Class<?> aClass = classLoader.loadClass(className);

            ClassDefinition classDefinition = new ClassDefinition(aClass, new ComponentScanAnnotationParserChangeClass().writeFile(classLoader, className));
            inst.redefineClasses(classDefinition);
            System.out.println("可进行热部署测试:ComponentScanAnnotationParser替换成功");
        } catch (ClassNotFoundException e) {
            System.out.println("热部署类注入失败, 无法进行热部署: " + e.getMessage());
        }

    }

    public static ClassLoader commonClassLoader;

    public static void handleClassLoader(String name, ClassLoader classLoader, ClassLoader parent) {
        if (name.equals("common")) {
            try {
                commonClassLoader = classLoader;
                webappLoaderClassChange(classLoader);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static List<String> PROJECT_NAME;

    public static void handleClassLoader(URLClassLoader urlClassLoader) {
        try {
            Class<?> aClass = commonClassLoader.loadClass("org.apache.catalina.loader.WebappClassLoaderBase");
            Method getContextName = aClass.getDeclaredMethod("getContextName");
            Object contextName = getContextName.invoke(urlClassLoader);
//            System.out.println("收到一个classLoader " + contextName + "   " + urlClassLoader + "   " + urlClassLoader.hashCode());
//
//            System.out.println("contextName " + contextName + "  0project_name:  " + PROJECT_NAME.get(0) + "   " + PROJECT_NAME.get(0).equals("/" + contextName));
            if (!PROJECT_NAME.get(0).equals(contextName)) {
                return;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
//        System.out.println(urlClassLoader + " !!!!!!!!!!  " + urlClassLoader.getResource(""));
        try {
//            Thread.currentThread().setContextClassLoader(urlClassLoader);
            initClass(urlClassLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改webappLoader文件来获取项目的classLoader
     *
     * @param classLoader
     * @throws Exception
     */
    public static void webappLoaderClassChange(ClassLoader classLoader) throws Exception {
        try {
            String className = "org.apache.catalina.loader.WebappLoader";

            byte[] bytes = new WebappLoaderChangeClass().writeFile(classLoader, className);
            Class<?> aClass = classLoader.loadClass(className);

            ClassDefinition classDefinition = new ClassDefinition(aClass, bytes);
            inst.redefineClasses(classDefinition);
            System.out.println("WebappLoader替换成功 " + aClass);
        } catch (ClassNotFoundException e) {
            System.out.println("类不存在, tomcat版本有问题, 无法注入 " + e.getMessage());
        }
    }


    public static void handleMySqlConfigChangeClass(Properties info, String url) {
        if (dbDetailInfoVo != null) {
            return;
        }

        dbDetailInfoVo = new DbDetailInfoVo();
        dbDetailInfoVo.setUrl(url);
        dbDetailInfoVo.setHost(info.getProperty("HOST"));
        dbDetailInfoVo.setPort(Integer.valueOf(info.getProperty("PORT")));
        dbDetailInfoVo.setUsername(info.getProperty("user"));
        dbDetailInfoVo.setPassword(info.getProperty("password"));
        dbDetailInfoVo.setDbName(info.getProperty("DBNAME"));
    }

    public static ThreadLocal<List<String>> sqlList = new ThreadLocal<>();

    public static void logSql(Object buffer, Object preparedStatement) {
        if (sqlList.get() == null) {
            sqlList.set(new LinkedList<>());
        }
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Class<?> bufferClass = contextClassLoader.loadClass("com.mysql.jdbc.Buffer");
            Method getByteBuffer = bufferClass.getMethod("getByteBuffer");
            Method getPosition = bufferClass.getMethod("getPosition");
            byte[] bufferContent = (byte[]) getByteBuffer.invoke(buffer);
            int bufferPosition = (int) getPosition.invoke(buffer);
            String sql = new String(bufferContent, 5, bufferPosition - 5);
            sqlList.get().add(sql);
            System.out.println("打印的 " + sql);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
