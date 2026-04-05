package io.github.nonoas.worktools.platform.ext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginLoader {

    private static final Logger LOG = LogManager.getLogger(PluginLoader.class);

    private static final String PLUGIN_YML = "META-INF/plugin.yml";

    private static final String DEFAULT_EXTERNAL_PLUGIN_DIR = "plugins";

    public static List<Plugin> loadClasspathPlugins() {
        LinkedHashMap<String, Plugin> instances = new LinkedHashMap<>();
        try {
            Enumeration<URL> resources = PluginLoader.class.getClassLoader().getResources(PLUGIN_YML);
            while (resources.hasMoreElements()) {
                URL configUrl = resources.nextElement();
                URL pluginRoot = getPluginRoot(configUrl);
                PluginClassLoader pluginLoader = new PluginClassLoader(
                        new URL[]{pluginRoot},
                        PluginLoader.class.getClassLoader()
                );

                try {
                    Plugin plugin = loadPlugin(configUrl, pluginRoot, pluginLoader, PluginSourceType.CLASSPATH);
                    registerPlugin(instances, plugin);
                } catch (Exception e) {
                    closeQuietly(pluginLoader);
                    LOG.error("Failed to load plugin from: {}", configUrl, e);
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to scan classpath plugins", e);
        }
        return new ArrayList<>(instances.values());
    }

    public static List<Plugin> loadExternalPlugins() {
        return loadExternalPlugins(getExternalPluginsDir());
    }

    public static List<Plugin> loadExternalPlugins(Path pluginDirectory) {
        LinkedHashMap<String, Plugin> instances = new LinkedHashMap<>();
        if (pluginDirectory == null || !Files.exists(pluginDirectory)) {
            return new ArrayList<>();
        }

        // 1. 先加载共享依赖库 (plugins/libs/)
        List<URL> sharedLibs = loadSharedLibraries(pluginDirectory);

        try (var pathStream = Files.list(pluginDirectory)) {
            pathStream.sorted()
                .filter(PluginLoader::isPluginArtifact)
                .forEach(path -> {
                    PluginClassLoader pluginLoader = null;
                    try {
                        // 2. 构建类加载器 URL：共享依赖在前，插件自身在后
                        List<URL> urls = new ArrayList<>(sharedLibs);
                        urls.add(path.toUri().toURL());

                        pluginLoader = new PluginClassLoader(
                                urls.toArray(new URL[0]),
                                PluginLoader.class.getClassLoader()
                        );

                        URL configUrl = resolveExternalConfigUrl(path);
                        if (configUrl == null) {
                            closeQuietly(pluginLoader);
                            return;
                        }

                        URL pluginRoot = path.toUri().toURL();
                        Plugin plugin = loadPlugin(configUrl, pluginRoot, pluginLoader, PluginSourceType.EXTERNAL_DIRECTORY);
                        plugin.setPath(path.toAbsolutePath().normalize().toString());
                        registerPlugin(instances, plugin);
                    } catch (Exception e) {
                        closeQuietly(pluginLoader);
                        LOG.error("Failed to load external plugin from: {}", path, e);
                    }
                });
        } catch (Exception e) {
            LOG.error("Failed to scan external plugin directory: {}", pluginDirectory, e);
        }
        return new ArrayList<>(instances.values());
    }

    /**
     * 加载共享依赖库目录 (plugins/libs/)
     */
    private static List<URL> loadSharedLibraries(Path pluginDirectory) {
        List<URL> sharedLibs = new ArrayList<>();
        Path libsDir = pluginDirectory.resolve("libs");

        if (!Files.exists(libsDir) || !Files.isDirectory(libsDir)) {
            return sharedLibs;
        }

        try (var pathStream = Files.list(libsDir)) {
            pathStream
                .filter(p -> p.toString().toLowerCase().endsWith(".jar"))
                .sorted()
                .forEach(p -> {
                    try {
                        sharedLibs.add(p.toUri().toURL());
                        LOG.debug("Loaded shared library: {}", p.getFileName());
                    } catch (Exception e) {
                        LOG.warn("Failed to load shared library: {}", p, e);
                    }
                });
        } catch (Exception e) {
            LOG.warn("Failed to scan shared libraries directory: {}", libsDir, e);
        }

        if (!sharedLibs.isEmpty()) {
            LOG.info("Loaded {} shared libraries from {}", sharedLibs.size(), libsDir);
        }
        return sharedLibs;
    }

    /**
     * 解析 plugin.yml 的 URL，提取出插件 JAR 的根路径
     */
    private static URL getPluginRoot(URL configUrl) throws Exception {
        String urlString = configUrl.toExternalForm();
        if (urlString.startsWith("jar:")) {
            return new URL(urlString.substring(4, urlString.indexOf("!/")));
        } else {
            return new URL(urlString.substring(0, urlString.length() - PLUGIN_YML.length()));
        }
    }

    private static Plugin loadPlugin(URL configUrl,
                                     URL pluginRoot,
                                     PluginClassLoader loader,
                                     PluginSourceType sourceType) throws Exception {
        try (InputStream in = configUrl.openStream()) {
            Yaml yaml = new Yaml();
            PluginYmlModel pluginModel = yaml.loadAs(in, PluginYmlModel.class);
            Plugin plugin = toPlugin(pluginModel, loader);
            plugin.setSourceType(sourceType);
            plugin.setState(PluginState.LOADED);
            if (plugin.getPath() == null || plugin.getPath().isBlank()) {
                plugin.setPath(resolvePluginPath(pluginRoot));
            }
            return plugin;
        } catch (Exception e) {
            closeQuietly(loader);
            throw e;
        }
    }

    private static String resolvePluginPath(URL pluginRoot) throws Exception {
        if (pluginRoot == null) {
            return null;
        }
        if ("file".equalsIgnoreCase(pluginRoot.getProtocol())) {
            return Paths.get(pluginRoot.toURI()).toAbsolutePath().normalize().toString();
        }
        return pluginRoot.toExternalForm();
    }

    private static Plugin toPlugin(PluginYmlModel pluginModel, ClassLoader loader) throws Exception {
        validatePluginModel(pluginModel);

        Plugin plugin = new Plugin();
        plugin.setClassLoader(loader);
        plugin.setId(pluginModel.getId());
        plugin.setName(pluginModel.getName());
        plugin.setVersion(pluginModel.getVersion());
        plugin.setMainClass(pluginModel.getMainClass());
        plugin.setPath(pluginModel.getPath());

        if (pluginModel.getMainClass() != null && !pluginModel.getMainClass().isBlank()) {
            Class<?> mainClass = loader.loadClass(pluginModel.getMainClass());
            if (!PluginService.class.isAssignableFrom(mainClass)) {
                throw new IllegalStateException("Plugin mainClass must implement PluginService: " + pluginModel.getMainClass());
            }
            PluginService service = (PluginService) mainClass.getDeclaredConstructor().newInstance();
            plugin.setService(service);
        }

        Map<Class<?>, List<?>> extensions = new HashMap<>();
        if (pluginModel.getExtensions() != null) {
            for (Map.Entry<String, List<String>> extension : pluginModel.getExtensions().entrySet()) {
                Class<?> interfaceClass = PluginLoader.class.getClassLoader().loadClass(extension.getKey());
                for (String implName : extension.getValue()) {
                    Class<?> implClass = loader.loadClass(implName);
                    if (interfaceClass.isAssignableFrom(implClass)) {
                        Object implObject = implClass.getDeclaredConstructor().newInstance();
                        addExtension(extensions, interfaceClass, implObject);
                    }
                }
            }
        }
        plugin.setExtensions(extensions);
        return plugin;
    }

    private static void validatePluginModel(PluginYmlModel pluginModel) {
        if (pluginModel == null) {
            throw new IllegalArgumentException("plugin.yml is empty");
        }
        if (pluginModel.getId() == null || pluginModel.getId().isBlank()) {
            throw new IllegalArgumentException("Plugin id is required");
        }
        if (pluginModel.getName() == null || pluginModel.getName().isBlank()) {
            throw new IllegalArgumentException("Plugin name is required");
        }
    }

    private static void registerPlugin(Map<String, Plugin> instances, Plugin plugin) {
        if (plugin == null) {
            return;
        }
        Plugin existed = instances.putIfAbsent(plugin.getId(), plugin);
        if (existed != null) {
            LOG.warn("Duplicate plugin id '{}', skip later plugin from {}", plugin.getId(), plugin.getPath());
            closeQuietly(plugin.getClassLoader());
        }
    }

    private static boolean isPluginArtifact(Path path) {
        if (Files.isDirectory(path)) {
            return Files.exists(path.resolve(Paths.get("META-INF", "plugin.yml")));
        }
        String fileName = path.getFileName().toString().toLowerCase();
        return Files.isRegularFile(path) && fileName.endsWith(".jar");
    }

    private static URL resolveExternalConfigUrl(Path path) throws Exception {
        if (Files.isDirectory(path)) {
            Path configPath = path.resolve(Paths.get("META-INF", "plugin.yml"));
            if (!Files.exists(configPath)) {
                return null;
            }
            return configPath.toUri().toURL();
        }

        try (JarFile jarFile = new JarFile(path.toFile())) {
            JarEntry entry = jarFile.getJarEntry(PLUGIN_YML);
            if (entry == null) {
                LOG.warn("Skip external jar without plugin descriptor: {}", path);
                return null;
            }
        }
        return new URL("jar:" + path.toUri().toURL().toExternalForm() + "!/" + PLUGIN_YML);
    }

    public static Path getExternalPluginsDir() {
        String override = System.getProperty("worktools.plugins.dir");
        if (override == null || override.isBlank()) {
            String envOverride = System.getenv("WORKTOOLS_PLUGINS_DIR");
            override = envOverride == null || envOverride.isBlank() ? DEFAULT_EXTERNAL_PLUGIN_DIR : envOverride;
        }
        return Paths.get(override).toAbsolutePath().normalize();
    }

    private static void closeQuietly(ClassLoader classLoader) {
        if (classLoader instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                LOG.warn("Failed to close plugin classloader", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void addExtension(Map<Class<?>, List<?>> extensions, Class<T> interfaceClass, Object implObject) {
        List<T> list = (List<T>) extensions.computeIfAbsent(interfaceClass, k -> new ArrayList<T>());
        list.add(interfaceClass.cast(implObject));
    }
}
