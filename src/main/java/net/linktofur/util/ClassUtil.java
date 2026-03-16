package net.linktofur.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

/**
 * @author LangYa466
 * @date 2026/3/16
 */
@Slf4j
public class ClassUtil {
    public static <T> List<Class<? extends T>> findSubClasses(String packageName, Class<T> parentClass) {
        List<Class<? extends T>> result = new ArrayList<>();
        var packagePath = packageName.replace('.', '/');
        var classLoader = Thread.currentThread().getContextClassLoader();

        try {
            var urls = classLoader.getResources(packagePath);
            while (urls.hasMoreElements()) {
                var url = urls.nextElement();
                switch (url.getProtocol()) {
                    case "file" -> scanFromDirectory(new File(url.toURI()), packageName, parentClass, result);
                    case "jar" -> scanFromJar(url, packagePath, parentClass, result);
                }
            }
        } catch (Exception e) {
            log.error("Failed to scan package: {}", packageName, e);
        }

        return result;
    }

    private static <T> void scanFromDirectory(File dir, String packageName,
                                              Class<T> parentClass, List<Class<? extends T>> result) {
        if (!dir.exists() || !dir.isDirectory()) return;

        var files = dir.listFiles();
        if (files == null) return;

        for (var file : files) {
            if (file.isDirectory()) {
                scanFromDirectory(file, packageName + "." + file.getName(), parentClass, result);
            } else if (file.getName().endsWith(".class")) {
                var className = packageName + "." + file.getName().replace(".class", "");
                tryLoadAndAdd(className, parentClass, result);
            }
        }
    }

    private static <T> void scanFromJar(URL url, String packagePath,
                                        Class<T> parentClass, List<Class<? extends T>> result) {
        try {
            JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
            jarFile.stream()
                    .filter(e -> e.getName().startsWith(packagePath) && e.getName().endsWith(".class"))
                    .map(e -> e.getName().replace('/', '.').replace(".class", ""))
                    .forEach(className -> tryLoadAndAdd(className, parentClass, result));
        } catch (Exception e) {
            log.error("Failed to scan jar: {}", url, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void tryLoadAndAdd(String className, Class<T> parentClass, List<Class<? extends T>> result) {
        try {
            var clazz = Class.forName(className);
            if (parentClass.isAssignableFrom(clazz)
                    && !clazz.isInterface()
                    && !Modifier.isAbstract(clazz.getModifiers())
                    && clazz != parentClass) {
                result.add((Class<? extends T>) clazz);
            }
        } catch (ClassNotFoundException e) {
            log.warn("Class not found: {}", className);
        }
    }
}