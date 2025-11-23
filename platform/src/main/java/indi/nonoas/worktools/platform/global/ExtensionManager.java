package indi.nonoas.worktools.platform.global;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExtensionManager {
    private final static Map<Class<?>, List<?>> extensionMap = new ConcurrentHashMap<>();

    public static Map<Class<?>, List<?>> getExtensionMap() {
        return extensionMap;
    }

    /**
     * 获取指定类型的扩展列表
     *
     * @param <T>   扩展类型
     * @param clazz 扩展类的Class对象
     * @return 扩展列表，如果不存在则返回空列表（不会返回null）
     */
    public static <T> List<T> getExtensions(Class<T> clazz) {
        Objects.requireNonNull(clazz, "Class parameter cannot be null");

        @SuppressWarnings("unchecked")
        List<T> extensions = (List<T>) extensionMap.get(clazz);

        return extensions != null ? extensions : Collections.emptyList();
    }

    /**
     * 动态类型注册方法 - 解决您遇到的问题
     */
    public static void registerExtension(Class<?> clazz, Object extension) {
        Objects.requireNonNull(clazz, "Class parameter cannot be null");
        Objects.requireNonNull(extension, "Extension instance cannot be null");

        if (!clazz.isInstance(extension)) {
            throw new IllegalArgumentException(
                    "Extension " + extension.getClass().getName() +
                            " is not an instance of " + clazz.getName());
        }

        // 使用原始类型绕过泛型检查，但在运行时是安全的
        @SuppressWarnings("unchecked")
        List<Object> extensions = (List<Object>) extensionMap.computeIfAbsent(
                clazz, k -> new CopyOnWriteArrayList<>());

        extensions.add(extension);
    }

    /**
     * 注销扩展实例
     *
     * @param <T>       扩展类型
     * @param clazz     扩展类的Class对象
     * @param extension 要注销的扩展实例
     * @return 如果成功移除返回true，否则返回false
     */
    public static <T> boolean unregisterExtension(Class<T> clazz, T extension) {
        Objects.requireNonNull(clazz, "Class parameter cannot be null");
        if (extension == null) return false;

        @SuppressWarnings("unchecked")
        List<T> extensions = (List<T>) extensionMap.get(clazz);

        return extensions != null && extensions.remove(extension);
    }

    /**
     * 检查是否包含指定类型的扩展
     *
     * @param clazz 扩展类的Class对象
     * @return 如果存在该类型的扩展返回true
     */
    public static boolean hasExtensions(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class parameter cannot be null");
        List<?> extensions = extensionMap.get(clazz);
        return extensions != null && !extensions.isEmpty();
    }

    /**
     * 清空所有扩展
     */
    public static void clear() {
        extensionMap.clear();
    }

    /**
     * 获取所有已注册的扩展类型
     *
     * @return 扩展类型的不可修改集合
     */
    public static Set<Class<?>> getRegisteredExtensionTypes() {
        return Collections.unmodifiableSet(extensionMap.keySet());
    }
}