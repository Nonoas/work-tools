package indi.nonoas.worktools.platform.ext;

import indi.nonoas.worktools.platform.ui.TaskHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PluginLoader {

    private static final Logger LOG = LogManager.getLogger(TaskHandler.class);

    private static final String PLUGIN_YML = "META-INF/plugin.yml";

    private static List<Plugin> instances;

    /**
     * 加载所有 plugin.yml 并实例化插件对象
     */
    public static List<Plugin> load() {
        if (instances != null) {
            return instances;
        }

        Yaml yaml = new Yaml();

        try {
            Enumeration<URL> resources = Thread.currentThread()
                    .getContextClassLoader()
                    .getResources(PLUGIN_YML);


            instances = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (InputStream in = url.openStream()) {
                    PluginYmlModel pluginModel = yaml.loadAs(in, PluginYmlModel.class);
                    instances.add(toPlugin(pluginModel));
                } catch (Exception e) {
                    LOG.error("Failed to load plugin from: {}", url, e);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }

        return instances;
    }

    private static Plugin toPlugin(PluginYmlModel pluginModel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Set<String> implSet = new HashSet<>();

        Plugin plugin = new Plugin();
        plugin.setId(pluginModel.getId());
        plugin.setName(pluginModel.getName());
        plugin.setVersion(pluginModel.getVersion());

        Map<Class<?>, List<?>> extensions = new HashMap<>();
        for (Map.Entry<String, List<String>> extension : pluginModel.getExtensions().entrySet()) {
            // 获取接口/父类的Class
            Class<?> interfaceClass = Class.forName(extension.getKey());
            for (String impl : extension.getValue()) {
                if (!implSet.add(impl)) {
                    continue;
                }
                // 获取实现类的Class
                Class<?> implClass = Class.forName(impl);

                // 修正：检查实现类是否实现了接口或继承了父类
                if (interfaceClass.isAssignableFrom(implClass)) {
                    Object implObject = implClass.getDeclaredConstructor().newInstance();
                    List impls = extensions.computeIfAbsent(interfaceClass, aClass -> new ArrayList<>());
                    impls.add(implObject);
                } else {
                    System.err.println("Warning: " + implClass.getName() +
                            " does not implement/extend " + interfaceClass.getName());
                }
            }
        }
        plugin.setExtensions(extensions);
        return plugin;
    }
}