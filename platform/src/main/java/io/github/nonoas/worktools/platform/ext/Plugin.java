package io.github.nonoas.worktools.platform.ext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 插件封装
 *
 * @author Nonoas
 * @datetime 2022/2/6 17:59
 */
public class Plugin {

    private String id;
    /**
     * 插件名称
     */
    private String name;
    /**
     * 插件版本
     */
    private String version;
    /**
     * 插件路径
     */
    private String path;

    /**
     * 主类类名
     */
    private String mainClass;
    /**
     * 实现类
     */
    private PluginService service;

    private Map<Class<?>, List<?>> extensions;

    private ClassLoader classLoader;

    private PluginSourceType sourceType = PluginSourceType.CLASSPATH;

    private PluginState state = PluginState.DISCOVERED;

    private String errorMessage;

    private PluginContext context;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public PluginService getService() {
        return service;
    }

    public void setService(PluginService service) {
        this.service = service;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public Map<Class<?>, List<?>> getExtensions() {
        return extensions;
    }

    public <T> List<T> getExtensionByType(Class<T> tClass) {
        return (List<T>) extensions.getOrDefault(tClass, Collections.emptyList());
    }

    public void setExtensions(Map<Class<?>, List<?>> extensions) {
        this.extensions = extensions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setClassLoader(ClassLoader loader) {
        this.classLoader = loader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public PluginSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(PluginSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public PluginState getState() {
        return state;
    }

    public void setState(PluginState state) {
        this.state = state;
    }

    public boolean isEnabled() {
        return state == PluginState.ENABLED;
    }

    public boolean isExternalPlugin() {
        return sourceType == PluginSourceType.EXTERNAL_DIRECTORY;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public PluginContext getContext() {
        return context;
    }

    public void setContext(PluginContext context) {
        this.context = context;
    }
}
