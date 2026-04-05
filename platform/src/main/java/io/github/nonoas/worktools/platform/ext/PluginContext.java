package io.github.nonoas.worktools.platform.ext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 插件运行时上下文
 */
public class PluginContext {

    private static final String DEFAULT_DATA_DIR = "plugin-data";

    private final Plugin plugin;
    private final Path pluginHome;
    private final Path dataDirectory;

    public PluginContext(Plugin plugin) {
        this.plugin = plugin;
        this.pluginHome = resolvePluginHome(plugin.getPath());
        this.dataDirectory = initDataDirectory(plugin.getId());
    }

    private static Path resolvePluginHome(String pluginPath) {
        if (pluginPath == null || pluginPath.isBlank()) {
            return null;
        }
        return Paths.get(pluginPath).toAbsolutePath().normalize();
    }

    private static Path initDataDirectory(String pluginId) {
        Path root = resolveDataRoot();
        Path dir = root.resolve(pluginId);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create plugin data directory: " + dir, e);
        }
        return dir;
    }

    private static Path resolveDataRoot() {
        String override = System.getProperty("worktools.plugin.data.dir");
        if (override == null || override.isBlank()) {
            override = DEFAULT_DATA_DIR;
        }
        Path root = Paths.get(override).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create plugin data root: " + root, e);
        }
        return root;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public String getPluginId() {
        return plugin.getId();
    }

    public String getPluginName() {
        return plugin.getName();
    }

    public Path getPluginHome() {
        return pluginHome;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public ClassLoader getClassLoader() {
        return plugin.getClassLoader();
    }
}
