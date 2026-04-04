package io.github.nonoas.worktools.platform.ext;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

/**
 * 插件专用类加载器：实现 Parent-Last (子类优先) 策略。
 * 解决多模块开发时，插件类被主类加载器抢先加载的问题。
 */
public class PluginClassLoader extends URLClassLoader {

    public PluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // 1. 首先检查类是否已经加载过
            Class<?> c = findLoadedClass(name);

            if (c == null) {
                // 2. 如果是 Java 核心库（java.*），必须交给父加载器（Bootstrap）
                // 否则会抛出 SecurityException
                if (name.startsWith("java.")
                        || name.startsWith("io.github.nonoas.worktools.platform.")
                        || name.startsWith("javax.")) {
                    return super.loadClass(name, resolve);
                }

                try {
                    // 3. 核心突破：先尝试在插件自己的 URL 路径中查找类
                    c = findClass(name);
                } catch (ClassNotFoundException e) {
                    // 4. 自己找不到，再委派给父加载器（AppClassLoader）
                    c = super.loadClass(name, resolve);
                }
            }

            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

    /**
     * 重写资源查找逻辑，确保 getResource 也能优先找到插件内部的资源（如 CSS/图片）
     */
    @Override
    public URL getResource(String name) {
        // 先在插件内找
        URL url = findResource(name);
        if (url == null) {
            // 找不到再问父类
            url = super.getResource(name);
        }
        return url;
    }

    /**
     * 重写多资源查找逻辑，确保 getResources 顺序也是插件优先
     */
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        // 这里的顺序决定了如果你扫描 plugin.yml，谁会排在前面
        Enumeration<URL> localUrls = findResources(name);
        Enumeration<URL> parentUrls = getParent().getResources(name);

        return new Enumeration<URL>() {
            @Override
            public boolean hasMoreElements() {
                return localUrls.hasMoreElements() || parentUrls.hasMoreElements();
            }

            @Override
            public URL nextElement() {
                if (localUrls.hasMoreElements()) {
                    return localUrls.nextElement();
                }
                return parentUrls.nextElement();
            }
        };
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getName() {
        return "worktools-plugin-class-loader";
    }
}