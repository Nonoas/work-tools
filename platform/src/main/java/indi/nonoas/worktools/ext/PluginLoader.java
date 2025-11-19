package indi.nonoas.worktools.ext;

import com.alibaba.fastjson2.JSON;
import indi.nonoas.worktools.ui.component.ExceptionAlter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * 插件加载类
 *
 * @author Nonoas
 * @datetime 2022/2/6 17:43
 */
public class PluginLoader {

    /**
     * 加载所有插件，返回一个插件数组
     */
    public static List<Plugin> load() throws Exception {
        List<Plugin> plugins = new ArrayList<>();

        File parentDir = new File("plugins");
        File[] files = parentDir.listFiles();
        if (null == files) {
            return Collections.emptyList();
        }

        // 此处从 plug-ins 文件夹下加载所有插件
        Plugin plugin;
        String path;
        for (File dir : files) {
            if (!dir.isDirectory()) {
                break;
            }
            // 插件jar包需与插件根目录同名
            path = dir + System.getProperty("file.separator") + dir.getName() + ".jar";
            plugin = readPlugin(path);
            plugins.add(plugin);
        }
        return plugins;
    }

    /**
     * 从插件jar包中读取 json 文件到 Plugin 类
     *
     * @param path jar 相对路径
     */
    private static Plugin readPlugin(String path) throws IOException {
        JarFile jarFile = new JarFile(path);
        ZipEntry zipEntry = jarFile.getEntry("META-INF/plugin.json");
        InputStream is = jarFile.getInputStream(zipEntry);
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String temp;
        StringBuilder sb = new StringBuilder();
        while ((temp = br.readLine()) != null) {
            sb.append(temp);
        }
        Plugin plugin = null;
        try {
            plugin = JSON.parseObject(sb.toString(), Plugin.class);
            plugin.setPath(path);

            URL[] url = new URL[]{new URL("file:" + path)};
            URLClassLoader loader = new URLClassLoader(url);
            Class<?> clazz = loader.loadClass(plugin.getMainClass());

            plugin.setService((PluginService) clazz.newInstance());
        } catch (Exception e) {
            new ExceptionAlter(e).show();
        }
        return plugin;
    }


}
