package io.github.nonoas.worktools.platform.ext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
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

    /**
     * 将功能级启用配置应用到插件运行态。
     * <p>
     * {@code func_setting} 表保存的是功能编码，这样一个插件即使提供多个
     * {@link FuncPaneFactory} 扩展，也可以在设置窗口中逐项显示和控制。
     * 但运行时扩展发现是按插件维度启停的，因此这里需要把“功能是否启用”
     * 推导为“插件是否启用”：只要插件下至少一个功能启用，插件就保持启用；
     * 当插件下所有功能都关闭时，才关闭该插件，避免继续向扩展管理器暴露功能。
     * <p>
     * 旧版本可能已经把配置保存为插件 id，例如
     * {@code io.github.nonoas.worktools.platform}。当找不到功能级配置时，
     * 会使用插件 id 对应的旧配置作为该插件下所有功能的默认状态；如果两类配置
     * 都不存在，则保持插件当前运行态，通常为加载后的默认启用状态。
     *
     * @param enableStates 启用配置，优先按功能编码取值，同时兼容旧的插件 id 键
     */
    public static synchronized void applyFunctionEnableStates(Map<String, Boolean> enableStates) {
        Map<String, Boolean> pluginEnableStates = resolvePluginEnableStates(enableStates);
        applyEnableStates(pluginEnableStates);
    }

    /**
     * 将功能级配置转换为插件级运行态配置。
     * <p>
     * 该方法只负责纯粹的状态推导，不直接修改插件生命周期。调用方可以通过返回值
     * 了解每个插件最终应该启用还是禁用，也可以继续传给
     * {@link #applyEnableStates(Map)} 完成实际启停。
     *
     * @param enableStates 启用配置，键可以是 {@link FuncPaneFactory#getCode()}，
     *                     也可以是旧版本保存的插件 id
     * @return 插件级启用配置，键为插件 id，值表示插件是否应保留在运行时扩展注册表中
     */
    public static Map<String, Boolean> resolvePluginEnableStates(Map<String, Boolean> enableStates) {
        Map<String, Boolean> pluginEnableStates = new HashMap<>();
        if (enableStates == null || enableStates.isEmpty()) {
            return pluginEnableStates;
        }

        for (Plugin plugin : getAll()) {
            Boolean legacyPluginState = enableStates.get(plugin.getId());
            List<FuncPaneFactory> functions = plugin.getExtensionByType(FuncPaneFactory.class);
            if (functions.isEmpty()) {
                if (legacyPluginState != null) {
                    pluginEnableStates.put(plugin.getId(), legacyPluginState);
                }
                continue;
            }

            boolean hasConfiguredFunction = legacyPluginState != null;
            boolean hasEnabledFunction = false;
            for (FuncPaneFactory function : functions) {
                Boolean functionState = enableStates.get(function.getCode());
                if (functionState != null) {
                    hasConfiguredFunction = true;
                    hasEnabledFunction = hasEnabledFunction || functionState;
                    continue;
                }
                if (legacyPluginState != null) {
                    hasEnabledFunction = hasEnabledFunction || legacyPluginState;
                    continue;
                }
                hasEnabledFunction = hasEnabledFunction || plugin.isEnabled();
            }

            if (hasConfiguredFunction) {
                pluginEnableStates.put(plugin.getId(), hasEnabledFunction);
            }
        }
        return pluginEnableStates;
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
