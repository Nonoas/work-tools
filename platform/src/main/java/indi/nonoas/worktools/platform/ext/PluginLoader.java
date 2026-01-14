package indi.nonoas.worktools.platform.ext;

import indi.nonoas.worktools.platform.ui.TaskHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginLoader {

    private static final Logger LOG = LogManager.getLogger(TaskHandler.class);

    private static final String PLUGIN_YML = "META-INF/plugin.yml";

    private static List<Plugin> instances;

    public static List<Plugin> load() {
        if (instances != null) return instances;

        instances = new ArrayList<>();
        Yaml yaml = new Yaml();

        try {
            // 这里依然用当前的加载器去寻找所有的 yml
            Enumeration<URL> resources = PluginLoader.class.getClassLoader().getResources(PLUGIN_YML);

            while (resources.hasMoreElements()) {
                URL configUrl = resources.nextElement();

                // 关键点：获取该 yml 所在的 JAR 或目录根路径
                URL pluginRoot = getPluginRoot(configUrl);

                // 为每个插件创建独立的加载器，并指定父加载器为主程序加载器
                PluginClassLoader pluginLoader = new PluginClassLoader(
                        new URL[]{pluginRoot},
                        PluginLoader.class.getClassLoader()
                );

                try (InputStream in = configUrl.openStream()) {
                    PluginYmlModel pluginModel = yaml.loadAs(in, PluginYmlModel.class);
                    // 传递这个 pluginLoader 进行类加载
                    instances.add(toPlugin(pluginModel, pluginLoader));
                } catch (Exception e) {
                    LOG.error("Failed to load plugin from: {}", configUrl, e);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }
        return instances;
    }

    /**
     * 解析 plugin.yml 的 URL，提取出插件 JAR 的根路径
     */
    private static URL getPluginRoot(URL configUrl) throws Exception {
        String urlString = configUrl.toExternalForm();
        if (urlString.startsWith("jar:")) {
            // jar:file:/path/to/plugin.jar!/META-INF/plugin.yml -> file:/path/to/plugin.jar
            return new URL(urlString.substring(4, urlString.indexOf("!/")));
        } else {
            // file:/path/to/classes/META-INF/plugin.yml -> file:/path/to/classes/
            return new URL(urlString.substring(0, urlString.length() - PLUGIN_YML.length()));
        }
    }

    private static Plugin toPlugin(PluginYmlModel pluginModel, ClassLoader loader) throws Exception {
        Plugin plugin = new Plugin();
        // 保存一下这个加载器，后续加载 CSS 或图标时要用到它
        plugin.setClassLoader(loader);

        plugin.setId(pluginModel.getId());
        plugin.setName(pluginModel.getName());
        plugin.setVersion(pluginModel.getVersion());

        Map<Class<?>, List<?>> extensions = new HashMap<>();
        for (Map.Entry<String, List<String>> extension : pluginModel.getExtensions().entrySet()) {
            // 使用插件专有的加载器加载接口和实现类
            Class<?> interfaceClass = PluginLoader.class.getClassLoader().loadClass(extension.getKey());

            for (String implName : extension.getValue()) {

                Class<?> implClass = loader.loadClass(implName);
                System.out.println("加载扩展，子类  " + implClass+" "+implClass.getClassLoader());
                System.out.println("加载扩展，父类  " + interfaceClass + " " + interfaceClass.getClassLoader());

                if (interfaceClass.isAssignableFrom(implClass)) {
                    // 实例化
                    Object implObject = implClass.getDeclaredConstructor().newInstance();
                    addExtension(extensions, interfaceClass, implObject);
                }
            }
        }
        plugin.setExtensions(extensions);
        return plugin;
    }

    @SuppressWarnings("unchecked")
    private static <T> void addExtension(Map<Class<?>, List<?>> extensions, Class<T> interfaceClass, Object implObject) {
        List<T> list = (List<T>) extensions.computeIfAbsent(interfaceClass, k -> new ArrayList<T>());
        list.add(interfaceClass.cast(implObject));
    }

}