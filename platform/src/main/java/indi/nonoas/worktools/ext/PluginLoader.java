package indi.nonoas.worktools.ext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author huangshengsheng
 * @date 2025/11/22 11:53
 */
public class PluginLoader {
    private static final String RESOURCE = "META-INF/plugin.yml";

    /**
     * 线程安全、可复用，真正解析只做一次
     */
    private static final Map<Class<?>, List<Class<?>>> CACHE =
            new ConcurrentHashMap<>();

    /**
     * 外部唯一入口
     */
    public static <T> List<Class<? extends T>> load(Class<T> spiType) {
        return CACHE.computeIfAbsent(spiType, k -> load0(spiType))
                .stream()
                .map(c -> (Class<? extends T>) c)
                .collect(Collectors.toList());
    }

    private static <T> List<Class<?>> load0(Class<T> spiType) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            List<Class<?>> list = new ArrayList<>();
            /* 1. 拿到所有 jar 里的 plugin.xml */
            Enumeration<URL> resources = cl.getResources(RESOURCE);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                /* 2. 解析单个 xml */
                parseOne(url, spiType, list, cl);
            }
            return list;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load plugin.xml for " + spiType, e);
        }
    }

    private static void parseOne(URL url, Class<?> spiType,
                                 List<Class<?>> out, ClassLoader cl) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(url.openStream());
        NodeList groups = doc.getElementsByTagName("spi");
        for (int i = 0; i < groups.getLength(); i++) {
            Element group = (Element) groups.item(i);
            String interfaceName = group.getAttribute("interface");
            if (!spiType.getName().equals(interfaceName)) {
                continue;          // 只关心目标接口
            }
            NodeList impls = group.getElementsByTagName("impl");
            for (int j = 0; j < impls.getLength(); j++) {
                String impl = impls.item(j).getTextContent().strip();
                Class<?> c = cl.loadClass(impl);
                out.add(c);
            }
        }
    }

    /* 工具方法：直接帮你实例化并返回对象列表 */
    public static <T> List<T> instantiate(Class<T> spiType) {
        return (List<T>) load(spiType).stream()
                .map(c -> {
                    try {
                        return c.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new IllegalStateException("Instantiate failed: " + c, e);
                    }
                })
                .toList();
    }
}