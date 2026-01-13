package indi.yiyi.stockmonitor.utils;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.yiyi.stockmonitor.data.Stock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 分组持久化配置管理
 * <pre>
 * groups.json 示例:
 * {
 *   "groups": [
 *     {
 *       "name": "科技股",
 *       "stocks": [
 *         {"marketCode": "0", "stockCode": "000001"},
 *         {"marketCode": "1", "stockCode": "600519"}
 *       ]
 *     }
 *   ]
 * }
 * </pre>
 *
 * @author Nonoas
 * @date 2025/8/27
 * @since 1.0.0
 */
public class GroupConfig {

    public static final Logger LOG = LogManager.getLogger(GroupConfig.class);

    private static final ObjectMapper mapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private static final Path DATA_DIR = Path.of(System.getProperty("user.home"), ".stockMonitor");
    private static final Path CONFIG_FILE = DATA_DIR.resolve("groups.json");


    public static class Group {
        private String name;
        private List<Stock> stocks = new ArrayList<>();

        public Group() {
        }

        public Group(String name) {
            this.name = name;
            this.stocks = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Stock> getStocks() {
            return stocks;
        }

        public void setStocks(List<Stock> stocks) {
            this.stocks = stocks;
        }
    }

    public static class Root {
        private List<Group> groups = new ArrayList<>();

        public List<Group> getGroups() {
            return groups;
        }

        public void setGroups(List<Group> groups) {
            this.groups = groups;
        }
    }

    // ==== 内部缓存 ====
    private static Root rootCache;

    static {
        load(); // 类加载时初始化
    }

    // ==== API ====

    /**
     * 读取配置文件
     */
    public static synchronized void load() {
        try {
            if (!Files.exists(CONFIG_FILE)) {
                Files.createDirectories(DATA_DIR);
                rootCache = new Root();
                save();
            } else {
                rootCache = mapper.readValue(CONFIG_FILE.toFile(), Root.class);
            }
            if (rootCache.groups.isEmpty()) {
                rootCache.groups.add(new Group("自选"));
                save();
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            rootCache = new Root();
        }
    }

    /**
     * 保存到文件
     */
    public static synchronized void save() {
        try {
            Files.createDirectories(DATA_DIR);
            mapper.writeValue(CONFIG_FILE.toFile(), rootCache);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * 获取所有分组
     */
    public static synchronized List<Group> getGroups() {
        return new ArrayList<>(rootCache.getGroups());
    }

    /**
     * 获取某个分组的股票
     */
    public static synchronized List<Stock> getStocksOf(String groupName) {
        return rootCache.getGroups().stream()
                .filter(g -> g.getName().equals(groupName))
                .findFirst()
                .map(Group::getStocks)
                .orElse(Collections.emptyList());
    }

    /**
     * 添加分组
     */
    public static synchronized boolean addGroup(String name) {
        if (rootCache.getGroups().stream().anyMatch(g -> g.getName().equals(name))) {
            return false; // 已存在
        }
        rootCache.getGroups().add(new Group(name));
        save();
        return true;
    }

    /**
     * 删除分组
     */
    public static synchronized boolean removeGroup(String name) {
        boolean removed = rootCache.getGroups().removeIf(g -> g.getName().equals(name));
        if (removed) save();
        return removed;
    }

    /**
     * 添加股票到指定分组
     */
    public static synchronized boolean addStock(String groupName, String marketCode, String stockCode) {
        for (Group g : rootCache.getGroups()) {
            if (g.getName().equals(groupName)) {
                boolean exists = g.getStocks().stream()
                        .anyMatch(s -> s.marketCode().equals(marketCode) && s.stockCode().equals(stockCode));
                if (exists) return false;
                g.getStocks().add(new Stock(marketCode, stockCode));
                save();
                return true;
            }
        }
        return false;
    }

    /**
     * 从指定分组删除股票
     */
    public static synchronized boolean removeStock(String groupName, String marketCode, String stockCode) {
        for (Group g : rootCache.getGroups()) {
            if (g.getName().equals(groupName)) {
                boolean removed = g.getStocks().removeIf(s ->
                        s.marketCode().equals(marketCode) && s.stockCode().equals(stockCode));
                if (removed) save();
                return removed;
            }
        }
        return false;
    }
}
