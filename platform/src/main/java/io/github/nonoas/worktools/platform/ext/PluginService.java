package io.github.nonoas.worktools.platform.ext;

/**
 * 外部插件入口接口
 *
 * @author Nonoas
 * @datetime 2022/2/6 17:25
 */
public interface PluginService {
    /**
     * 插件加载完成后调用一次。
     */
    default void onLoad(PluginContext context) {
    }

    /**
     * 插件启用时调用。
     */
    default void onEnable() {
        service();
    }

    /**
     * 插件停用时调用。
     */
    default void onDisable() {
    }

    /**
     * 插件卸载前调用一次。
     */
    default void onUnload() {
    }

    /**
     * 兼容旧接口。
     */
    @Deprecated
    default void service() {
    }
}
