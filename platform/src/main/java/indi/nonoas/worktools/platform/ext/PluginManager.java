package indi.nonoas.worktools.platform.ext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author huangshengsheng
 * @date 2025/12/12 20:56
 */
public class PluginManager {

    /**
     * 存放所有插件，K: 插件 id  V: 插件类
     */
    private static final ConcurrentHashMap<String, Plugin> pluginMap = new ConcurrentHashMap<>();

    public static void loadPlugins() {
        List<Plugin> plugins = PluginLoader.load();
        for (Plugin plugin : plugins) {
            pluginMap.put(plugin.getId(), plugin);
        }
    }

    public static List<Plugin> getAll() {
        return new ArrayList<>(pluginMap.values());
    }

    public static Plugin getPluginById(String id) {
        return pluginMap.get(id);
    }
}