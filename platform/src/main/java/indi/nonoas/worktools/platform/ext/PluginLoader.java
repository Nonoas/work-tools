package indi.nonoas.worktools.platform.ext;

import indi.nonoas.worktools.platform.global.ExtensionManager;
import indi.nonoas.worktools.platform.ui.TaskHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
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

            Set<String> implSet = new HashSet<>();
            instances = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (InputStream in = url.openStream()) {
                    Plugin plugin = yaml.loadAs(in, Plugin.class);
                    instances.add(plugin);
                    for (Map.Entry<String, List<String>> extension : plugin.getExtensions().entrySet()) {
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

                                // 修正：使用接口Class和实现类实例注册
                                // 注意：这里需要将ExtensionManager改为实例方法调用，或者保持静态但需要调整
                                ExtensionManager.registerExtension(interfaceClass, implObject);
                            } else {
                                System.err.println("Warning: " + implClass.getName() +
                                        " does not implement/extend " + interfaceClass.getName());
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Failed to load plugin from: {}", url, e);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }

        return instances;
    }
}