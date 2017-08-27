package org.tendons.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
/**
 * 
 * @author: chengweixiong@uworks.cc
 * @date: 2017年7月23日 下午4:39:07
 */
public class ClassUtils {

      private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtils.class);

      /**
       * 获取当前类加载器
       *
       * @return
       */
      public static ClassLoader getClassLoader() {
          return Thread.currentThread().getContextClassLoader();
      }

      /**
       * 加载类，默认顺道初始化
       *
       * @param className
       * @return
       */
      public static Class<?> loadClass(String className) {
          return loadClass(className, true);
      }

      /**
       * 加载类
       *
       * @param className
       * @param isInitialized
       * @return
       */
      public static Class<?> loadClass(String className, boolean isInitialized) {
          Class<?> cls = null;
          try {
              cls = Class.forName(className, isInitialized, getClassLoader());
          } catch (ClassNotFoundException e) {
              LOGGER.error("load class failure", e);
              e.printStackTrace();
          }
          return cls;
      }

      /**
       * 获取指定包名下的所有的类
       *
       * @param packageName
       * @return
       */
      public static Set<Class<?>> getClassSet(String packageName) {
          Set<Class<?>> classSet = new HashSet<Class<?>>();

          try {
              //获取包名的路径
              Enumeration<URL> urls = getClassLoader().getResources(packageName.replace(".", "/"));
              while (urls.hasMoreElements()) {
                  //取出包下的类路径
                  URL url = urls.nextElement();
                  if (url != null) {
                      //获取url协议
                      String protocol = url.getProtocol();
                      if ("file".equalsIgnoreCase(protocol)) {
                          String packagePath = url.getPath().replaceAll("%20", " ");
                          addClass(classSet, packagePath, packageName);
                      } else if ("jar".equalsIgnoreCase(protocol)) {
                          JarURLConnection jarUrlConnection = (JarURLConnection) url.openConnection();
                          if (jarUrlConnection != null) {
                              JarFile jarFile = jarUrlConnection.getJarFile();
                              if (jarFile != null) {
                                  Enumeration<JarEntry> jarEntries = jarFile.entries();
                                  while (jarEntries.hasMoreElements()) {
                                      JarEntry jarEntry = jarEntries.nextElement();
                                      String jarEntryName = jarEntry.getName();
                                      if (jarEntryName.endsWith(".class")) {
                                          String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replaceAll("/", ".");
                                          doAddClass(classSet, className);
                                      }
                                  }
                              }
                          }
                      }
                  }
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
          return classSet;
      }

      /**
       * 类加载器，添加.Class文件
       *
       * @param classSet
       * @param packagePath
       * @param packageName
       */
      private static void addClass(Set<Class<?>> classSet, String packagePath, String packageName) {
          File[] files = new File(packagePath).listFiles(new FileFilter() {
            
            @Override
            public boolean accept(File file) {
              return (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory();
            }
          });
          /*
          Arrays.asList(files).stream()
                  .filter(file -> file.isFile())
                  .forEach(file -> {
                      String fileName = file.getName();
                      String className = fileName.substring(0, fileName.lastIndexOf("."));
                      if (StringUtils.isNotBlank(className)) {
                          className = packageName + "." + className;
                      }
                      //类加载
                      doAddClass(classSet, className);
                  });

          Arrays.asList(files).stream()
                  .filter(file -> !file.isFile())
                  .forEach(file -> {
                      String fileName = file.getName();
                      String subPackagePath = fileName;
                      if (StringUtils.isNotBlank(packagePath)) {
                          //补全当前路径
                          subPackagePath = packagePath + "/" + subPackagePath;
                      }
                      String subPackageName = fileName;
                      if (StringUtils.isNotBlank(packageName)) {
                          subPackageName = packageName + "." + subPackageName;
                      }
                      //递归遍历文件夹下的目录
                      addClass(classSet, subPackagePath, subPackageName);
                  });
          */

          final List<File> fileList =  Arrays.asList(files);
          for (File file : fileList) {
                String fileName = file.getName();
                if (file.isFile()) {
                    //取出.class之前的类名
                    String className = fileName.substring(0, fileName.lastIndexOf("."));
                    if (StringUtils.isNotBlank(className)) {
                        className = packageName + "." + className;
                    }
                    //类加载
                    doAddClass(classSet, className);
                } else {
                    //目录
                    String subPackagePath = fileName;
                    if (StringUtils.isNotBlank(packagePath)) {
                        //补全当前路径
                        subPackagePath = packagePath + "/" + subPackagePath;
                    }
                    String subPackageName = fileName;
                    if (StringUtils.isNotBlank(packageName)) {
                        subPackageName = packageName + "." + subPackageName;
                    }
                    //递归遍历文件夹下的目录
                    addClass(classSet, subPackagePath, subPackageName);
                }
          }
      }
      /**
       * 添加类到类集合
       *
       * @param classSet
       * @param className
       */
      private static void doAddClass(Set<Class<?>> classSet, String className) {
          Class<?> cls = loadClass(className, false);
          classSet.add(cls);
      }
  }
