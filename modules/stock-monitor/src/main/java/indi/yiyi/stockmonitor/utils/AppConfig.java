package indi.yiyi.stockmonitor.utils;

import github.nonoas.jfx.flat.ui.config.ConfigManager;

/**
 * @author Nonoas
 * @date 2025/11/2
 * @since
 */
public class AppConfig {
    private static ConfigManager configManager;

    public static synchronized void load() {
        if (null == configManager) {
            configManager = new ConfigManager("config", "stockMonitor");
        }
    }

    public static ConfigManager getConfigManager() {
        load();
        return configManager;
    }
}
