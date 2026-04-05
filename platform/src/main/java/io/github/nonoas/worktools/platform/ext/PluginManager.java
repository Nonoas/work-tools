package io.github.nonoas.worktools.platform.ext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件注册表 + 生命周期管理
 */
public class PluginManager {

    private static final Logger LOG = LogManager.getLogger(PluginManager.class);

    /**
     * 存放所有插件，K: 插件 id  V: 插件实例
     */
    private static final ConcurrentHashMap<String, Plugin> pluginMap = new ConcurrentHashMap<>();

    private static volatile boolean initialized = false;

    public static synchronized void loadPlugins() {
        if (initialized) {
            return;
        }
        registerPlugins(PluginLoader.loadClasspathPlugins());
        registerPlugins(PluginLoader.loadExternalPlugins());
        enableAllPlugins();
        initialized = true;
    }

    public static List<Plugin> getAll() {
        ArrayList<Plugin> list = new ArrayList<>(pluginMap.values());
        list.sort(Comparator.comparing(Plugin::getId));
        return list;
    }

    public static Plugin getPluginById(String id) {
        return pluginMap.get(id);
    }

    public static List<Plugin> getEnabledPlugins() {
        ArrayList<Plugin> plugins = new ArrayList<>();
        for (Plugin plugin : pluginMap.values()) {
            if (plugin.isEnabled()) {
                plugins.add(plugin);
            }
        }
        plugins.sort(Comparator.comparing(Plugin::getId));
        return plugins;
    }

    /**
     * 重新扫描外部插件目录。
     * 现有 classpath 插件保持不动，外部插件会按新目录状态重新加载。
     */
    public static synchronized void reloadExternalPlugins() {
        List<String> externalPluginIds = new ArrayList<>();
        for (Plugin plugin : pluginMap.values()) {
            if (plugin.isExternalPlugin()) {
                externalPluginIds.add(plugin.getId());
            }
        }
        externalPluginIds.sort(String::compareTo);
        for (String pluginId : externalPluginIds) {
            unloadPlugin(pluginId);
        }

        registerPlugins(PluginLoader.loadExternalPlugins());
        enableAllPlugins();
    }

    public static synchronized void applyEnableStates(Map<String, Boolean> enableStates) {
        if (enableStates == null || enableStates.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Boolean> entry : enableStates.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                enablePlugin(entry.getKey());
            } else {
                disablePlugin(entry.getKey());
            }
        }
    }

    public static synchronized boolean enablePlugin(String pluginId) {
        Plugin plugin = pluginMap.get(pluginId);
        if (plugin == null) {
            return false;
        }
        if (plugin.getState() == PluginState.ENABLED) {
            return true;
        }
        if (plugin.getState() == PluginState.UNLOADED || plugin.getState() == PluginState.FAILED) {
            return false;
        }

        try {
            if (plugin.getService() != null) {
                plugin.getService().onEnable();
            }
            plugin.setState(PluginState.ENABLED);
            plugin.setErrorMessage(null);
            return true;
        } catch (Exception e) {
            plugin.setState(PluginState.FAILED);
            plugin.setErrorMessage(e.getMessage());
            LOG.error("Failed to enable plugin: {}", pluginId, e);
            return false;
        }
    }

    public static synchronized boolean disablePlugin(String pluginId) {
        Plugin plugin = pluginMap.get(pluginId);
        if (plugin == null) {
            return false;
        }
        if (plugin.getState() != PluginState.ENABLED) {
            return true;
        }

        try {
            if (plugin.getService() != null) {
                plugin.getService().onDisable();
            }
            plugin.setState(PluginState.DISABLED);
            return true;
        } catch (Exception e) {
            plugin.setState(PluginState.FAILED);
            plugin.setErrorMessage(e.getMessage());
            LOG.error("Failed to disable plugin: {}", pluginId, e);
            return false;
        }
    }

    public static synchronized boolean unloadPlugin(String pluginId) {
        Plugin plugin = pluginMap.get(pluginId);
        if (plugin == null) {
            return false;
        }
        disablePlugin(pluginId);

        try {
            if (plugin.getService() != null) {
                plugin.getService().onUnload();
            }
        } catch (Exception e) {
            plugin.setState(PluginState.FAILED);
            plugin.setErrorMessage(e.getMessage());
            LOG.error("Failed to unload plugin: {}", pluginId, e);
        } finally {
            closePluginClassLoader(plugin);
            plugin.setState(PluginState.UNLOADED);
            pluginMap.remove(pluginId);
        }
        return true;
    }

    public static synchronized void shutdown() {
        List<String> ids = new ArrayList<>(pluginMap.keySet());
        ids.sort(String::compareTo);
        for (String id : ids) {
            unloadPlugin(id);
        }
        initialized = false;
    }

    private static void registerPlugins(Collection<Plugin> plugins) {
        for (Plugin plugin : plugins) {
            if (pluginMap.containsKey(plugin.getId())) {
                LOG.warn("Plugin id '{}' already exists, keep current plugin and skip {}", plugin.getId(), plugin.getPath());
                closePluginClassLoader(plugin);
                continue;
            }
            if (!loadPlugin(plugin)) {
                continue;
            }
            pluginMap.put(plugin.getId(), plugin);
        }
    }

    private static boolean loadPlugin(Plugin plugin) {
        try {
            PluginContext context = new PluginContext(plugin);
            plugin.setContext(context);
            if (plugin.getService() != null) {
                plugin.getService().onLoad(context);
            }
            plugin.setState(PluginState.LOADED);
            plugin.setErrorMessage(null);
            return true;
        } catch (Exception e) {
            plugin.setState(PluginState.FAILED);
            plugin.setErrorMessage(e.getMessage());
            closePluginClassLoader(plugin);
            LOG.error("Failed to initialize plugin: {}", plugin.getId(), e);
            return false;
        }
    }

    private static void enableAllPlugins() {
        for (Plugin plugin : getAll()) {
            if (plugin.getState() == PluginState.LOADED || plugin.getState() == PluginState.DISABLED) {
                enablePlugin(plugin.getId());
            }
        }
    }

    private static void closePluginClassLoader(Plugin plugin) {
        ClassLoader loader = plugin.getClassLoader();
        if (loader instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                LOG.warn("Failed to close classloader for plugin {}", plugin.getId(), e);
            }
        }
    }
}
