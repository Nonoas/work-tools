package indi.nonoas.worktools.ext;

import indi.nonoas.worktools.ui.TaskHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PluginLoader {

    private static Logger LOG = LogManager.getLogger(TaskHandler.class);

    private static final String PLUGIN_YML = "META-INF/plugin.yml";

    /**
     * 加载所有 plugin.yml 并实例化插件对象
     */
    public static List<Object> load() {
        List<Object> instances = new ArrayList<>();
        Yaml yaml = new Yaml();

        try {
            Enumeration<URL> resources = Thread.currentThread()
                    .getContextClassLoader()
                    .getResources(PLUGIN_YML);

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (InputStream in = url.openStream()) {
                    Plugin plugin = yaml.loadAs(in, Plugin.class);
                    if (plugin != null && plugin.getMainClass() != null) {
                        try {
                            Class<?> clazz = Class.forName(plugin.getMainClass());
                            Object instance = clazz.getDeclaredConstructor().newInstance();
                            instances.add(instance);
                        } catch (Exception e) {
                            LOG.error(e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }

        return instances;
    }
}