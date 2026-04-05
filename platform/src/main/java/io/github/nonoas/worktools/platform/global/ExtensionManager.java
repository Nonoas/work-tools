package io.github.nonoas.worktools.platform.global;

import io.github.nonoas.worktools.platform.ext.Plugin;
import io.github.nonoas.worktools.platform.ext.PluginManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExtensionManager {

    /**
     * 获取指定类型的扩展列表
     *
     * @param <T>   扩展类型
     * @param clazz 扩展类的Class对象
     * @return 扩展列表，如果不存在则返回空列表（不会返回null）
     */
    public static <T> List<T> getExtensions(Class<T> clazz) {
        List<Plugin> pluginList = PluginManager.getEnabledPlugins();
        List<T> result = new ArrayList<>();

        Set<String> classNames = new HashSet<>();

        for (Plugin plugin : pluginList) {
            List<T> extensionList = plugin.getExtensionByType(clazz);
            for (T e : extensionList) {
                if (classNames.add(e.getClass().getName())) {
                    result.add(e);
                }
            }
        }
        return result;
    }
}
