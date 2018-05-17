package com.lcw.util;

import com.lcw.model.ParameterModel;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.lang.time.DateUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lancw
 */
public class ReflectUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectUtil.class.getName());
    private static final HashMap<String, List<MethodNode>> CACHE = new HashMap<>();

    /**
     * 获取方法参数名称
     *
     * @param theMethod
     * @return
     * @throws Exception
     */
    public static List<ParameterModel> getParameterNames(Method theMethod) throws Exception {
        Class<?> declaringClass = theMethod.getDeclaringClass();
        String key = declaringClass.getName();
        String constructorDescriptor = Type.getMethodDescriptor(theMethod);
        if (CACHE.get(key) == null) {
            ClassLoader declaringClassLoader = declaringClass.getClassLoader();

            Type declaringType = Type.getType(declaringClass);
            String url = declaringType.getInternalName() + ".class";

            InputStream classFileInputStream = declaringClassLoader.getResourceAsStream(url);
            if (classFileInputStream == null) {
                throw new IllegalArgumentException("The constructor's class loader cannot find the bytecode that defined the constructor's class (URL: " + url + ")");
            }

            ClassNode classNode;
            try {
                classNode = new ClassNode();
                ClassReader classReader = new ClassReader(classFileInputStream);
                classReader.accept(classNode, 0);
            } finally {
                classFileInputStream.close();
            }
            CACHE.put(key, classNode.methods);
        }

        List<MethodNode> methods = CACHE.get(key);
        for (MethodNode method : methods) {
            if (method.name.equals(theMethod.getName()) && method.desc.equals(constructorDescriptor)) {
                Type[] argumentTypes = Type.getArgumentTypes(method.desc);
                List<ParameterModel> parameterNames = new ArrayList<>(argumentTypes.length);

                List<LocalVariableNode> localVariables = method.localVariables;
                if (localVariables == null) {
                    for (int i = 0; i < argumentTypes.length; i++) {
                        Type at = argumentTypes[i];
                        String name = theMethod.getParameters()[i].getName();
                        parameterNames.add(new ParameterModel(name, at.toString(), theMethod, i));
                    }
                } else {
                    Collections.sort(localVariables, (LocalVariableNode o1, LocalVariableNode o2) -> Integer.valueOf(o1.index).compareTo(o2.index));
                    for (int i = 1; i <= argumentTypes.length; i++) {
                        LocalVariableNode lvn = localVariables.get(i);
                        parameterNames.add(new ParameterModel(lvn.name, lvn.desc, theMethod, i - 1));
                    }
                }
                return parameterNames;
            }
        }
        return null;
    }

    /**
     * 从jar包中查找指定接口的一个实现类 非反射
     *
     * @param interfaceClass
     * @param jarPath
     * @return
     * @throws Exception
     */
    public static Class findImplementFromJar(Class interfaceClass, URL jarPath) throws Exception {
        URL url = new URL("jar:" + jarPath.toString() + "!/");
        JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
        JarFile jarFile = jarConnection.getJarFile();
        Enumeration<JarEntry> je = jarFile.entries();
        boolean flag = false;
        while (je.hasMoreElements()) {
            JarEntry e = je.nextElement();
            String n = e.getName();
            FileTime ft = e.getLastModifiedTime();
            if (DateUtils.addDays(new Date(), -100).getTime() - ft.toMillis() > 0) {
                LOGGER.info("jar文件时间超过100天，跳过查找实现类：" + jarFile.getName().substring(jarFile.getName().lastIndexOf("\\")) + ft.toString());
                return null;
            } else {
                if (!flag) {
                    flag = true;
                    LOGGER.info("在" + jarFile.getName().substring(jarFile.getName().lastIndexOf("\\")) + "中查找实现类");
                }
            }
            if (n.endsWith(".class")) {
                n = n.substring(0, n.length() - 6);
                n = n.replace('/', '.');
                Class currentClass = ClassLoader.getSystemClassLoader().loadClass(n);
                if (interfaceClass.isAssignableFrom(currentClass) && !n.equals(interfaceClass.getName())) {
                    return currentClass;
                }
            }
        }
        return null;
    }

    /**
     * 添加classPath
     *
     * @param url
     * @throws Exception
     */
    public static void addURL(URL url) throws Exception {
        Class[] parameters = new Class[]{URL.class};
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{url});
        } catch (Throwable t) {
            LOGGER.error("Error, could not add URL to system classloader", t);
        }
    }

    /**
     * 获取泛型类型
     *
     * @param t
     * @param index
     * @return
     */
    public static Class getParameterizedTypes(java.lang.reflect.Type t, int index) {
        if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) t;
            Class genericClazz = (Class) pt.getActualTypeArguments()[index];
            return genericClazz;
        }
        return null;
    }
    private static final String[] NOT_ANALYSIS_CLASS = new String[]{"java.lang", "java.math", "java.sql.Date", "java.util.Date"};

    /**
     * 检查是否不需要解析的class类型
     *
     * @param c
     * @return
     */
    public static boolean isNotAnalysisClass(String c) {
        for (String s : NOT_ANALYSIS_CLASS) {
            if (c.contains(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析class的所有字段转为伪json
     *
     * @param c    要转换的class
     * @param deep 深度
     * @return
     */
    public static String classToJson(Class c, int deep) {
        StringBuilder sb = new StringBuilder();
        Field[] fs = c.getDeclaredFields();
        if (deep > 5 || isNotAnalysisClass(c.getName())) {
            return "";
        }
        sb.append("{");
        for (int i = 0; i < fs.length; i++) {
            Field f = fs[i];
            Class t = f.getType();
            if (t.isPrimitive() || t.isEnum() || isNotAnalysisClass(t.getTypeName())) {
                sb.append(f.getName()).append(":''");
            } else if (t.isArray()) {
                sb.append(f.getName()).append(":[]");
            } else if (t.isInterface()) {
                if (t.isAssignableFrom(List.class) || t.isAssignableFrom(Set.class)) {
                    Class gc = ReflectUtil.getParameterizedTypes(f.getGenericType(), 0);
                    if (deep < 5) {
                        sb.append(f.getName()).append(":[").append(classToJson(gc, deep + 1)).append("]");
                    } else {
                        sb.append(f.getName()).append(":[").append(gc).append("]");
                    }
                } else if (t.isAssignableFrom(Map.class)) {
                    Class gc = ReflectUtil.getParameterizedTypes(f.getGenericType(), 1);
                    if (deep < 3) {
                        sb.append(f.getName()).append(":{key:").append(classToJson(gc, deep + 1)).append("}");
                    } else {
                        sb.append(f.getName()).append(":{key:").append(gc).append("}");
                    }
                } else {
                    sb.append(f.getName()).append(":").append(classToJson(t, deep + 1));
                }
            } else {
                sb.append(f.getName()).append(":").append(classToJson(t, deep + 1));
            }
            if (i != fs.length - 1) {
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
