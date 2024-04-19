package indi.nonoas.worktools.ext;

/**
 * 插件封装类
 *
 * @author Nonoas
 * @datetime 2022/2/6 17:59
 */
public class Plugin {
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
}
