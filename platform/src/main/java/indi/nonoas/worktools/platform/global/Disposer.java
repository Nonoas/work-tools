package indi.nonoas.worktools.platform.global;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 简化版 Disposer - 核心资源管理器
 * 
 * 主要职责：
 * 1. 管理 Disposable 对象的父子关系
 * 2. 自动释放资源和其所有子资源
 * 3. 提供释放状态检查
 * 4. 支持释放回调函数
 */
public class Disposer {
    
    private static final Disposer INSTANCE = new Disposer();
    
    // 存储所有已注册的 Disposable 对象及其信息
    private final Map<Disposable, DisposableInfo> disposables = new ConcurrentHashMap<>();
    
    // 调试日志
    private boolean debugMode = false;
    
    private Disposer() {
    }
    
    /**
     * 获取 SimplifiedDisposer 的单例实例
     */
    public static Disposer getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册一个新的 Disposable 对象
     * 
     * @param disposable 要注册的对象
     * @param name 对象的名称（用于调试和日志）
     * @return Disposable 对象本身
     */
    public static Disposable register(Disposable disposable, String name) {
        getInstance().doRegister(disposable, null, name);
        return disposable;
    }
    
    /**
     * 注册一个 Disposable 对象到指定的父对象
     * 当父对象释放时，此对象也会自动释放
     * 
     * @param parent 父 Disposable 对象
     * @param child 子 Disposable 对象
     * @param name 子对象的名称
     * @return 子对象本身
     */
    public static Disposable register(Disposable parent, Disposable child, String name) {
        getInstance().doRegister(child, parent, name);
        return child;
    }
    
    /**
     * 注册一个 Disposable 对象（自动生成名称）
     */
    public static Disposable register(Disposable disposable) {
        return register(disposable, disposable.getClass().getSimpleName());
    }
    
    /**
     * 注册子 Disposable 到父对象
     */
    public static Disposable register(Disposable parent, Disposable child) {
        return register(parent, child, child.getClass().getSimpleName());
    }
    
    /**
     * 内部方法：执行注册逻辑
     */
    private synchronized void doRegister(Disposable disposable, Disposable parent, String name) {
        if (disposable == null) {
            throw new IllegalArgumentException("Disposable 不能为 null");
        }
        
        if (disposables.containsKey(disposable)) {
            log("警告: " + name + " 已经被注册过了");
            return;
        }
        
        DisposableInfo info = new DisposableInfo(disposable, parent, name);
        disposables.put(disposable, info);
        
        if (parent != null) {
            DisposableInfo parentInfo = disposables.get(parent);
            if (parentInfo != null) {
                parentInfo.addChild(disposable);
            }
        }
        
        log("注册: " + name);
    }
    
    /**
     * 创建一个新的顶级 Disposable 对象
     * 
     * @param name 对象的名称
     * @return 新创建的 Disposable 对象
     */
    public static Disposable newDisposable(String name) {
        return register(new DefaultDisposable(), name);
    }
    
    /**
     * 创建一个新的子 Disposable 对象
     * 
     * @param parent 父对象
     * @param name 子对象的名称
     * @return 新创建的子 Disposable 对象
     */
    public static Disposable newDisposable(Disposable parent, String name) {
        return register(parent, new DefaultDisposable(), name);
    }
    
    /**
     * 释放一个 Disposable 对象及其所有子对象
     * 
     * @param disposable 要释放的对象
     */
    public static void dispose(Disposable disposable) {
        getInstance().doDispose(disposable);
    }
    
    /**
     * 内部方法：执行释放逻辑
     */
    private synchronized void doDispose(Disposable disposable) {
        if (disposable == null) {
            return;
        }
        
        DisposableInfo info = disposables.get(disposable);
        if (info == null) {
            log("警告: " + disposable + " 未被注册");
            return;
        }
        
        if (info.isDisposed) {
            log("警告: " + info.name + " 已被释放，忽略重复释放");
            return;
        }
        
        log("释放: " + info.name);
        info.isDisposed = true;
        
        // 首先释放所有子对象
        for (Disposable child : new ArrayList<>(info.children)) {
            doDispose(child);
        }
        
        // 执行释放前的回调
        for (Runnable callback : info.disposeCallbacks) {
            try {
                callback.run();
            } catch (Exception e) {
                log("错误: 释放回调执行失败 - " + e.getMessage());
            }
        }
        
        // 调用对象自己的 dispose() 方法
        try {
            disposable.dispose();
        } catch (Exception e) {
            log("错误: " + info.name + ".dispose() 抛出异常 - " + e.getMessage());
        }
        
        // 从注册表中移除
        disposables.remove(disposable);
    }
    
    /**
     * 检查 Disposable 是否已被释放
     * 
     * @param disposable 要检查的对象
     * @return 如果已释放返回 true，否则返回 false
     */
    public static boolean isDisposed(Disposable disposable) {
        DisposableInfo info = getInstance().disposables.get(disposable);
        return info == null || info.isDisposed;
    }
    
    /**
     * 添加释放前的回调函数
     * 当对象释放时，所有回调函数都会被执行
     * 
     * @param disposable 目标对象
     * @param callback 回调函数
     */
    public static void addDisposeCallback(Disposable disposable, Runnable callback) {
        DisposableInfo info = getInstance().disposables.get(disposable);
        if (info != null && !info.isDisposed) {
            info.addDisposeCallback(callback);
        }
    }
    
    /**
     * 启用调试模式
     * 启用后会打印所有注册和释放的日志
     */
    public static void setDebugMode(boolean enabled) {
        getInstance().debugMode = enabled;
    }
    
    /**
     * 获取所有已注册的 Disposable 对象数量（仅用于测试和调试）
     */
    public static int getRegisteredCount() {
        return getInstance().disposables.size();
    }
    
    /**
     * 清除所有已注册的 Disposable 对象（仅用于测试）
     */
    public static void clear() {
        synchronized (getInstance()) {
            List<Disposable> toDispose = new ArrayList<>(getInstance().disposables.keySet());
            for (Disposable d : toDispose) {
                getInstance().doDispose(d);
            }
            getInstance().disposables.clear();
        }
    }
    
    /**
     * 获取 Disposable 的调试信息（仅用于开发调试）
     */
    public static String getDebugInfo(Disposable disposable) {
        DisposableInfo info = getInstance().disposables.get(disposable);
        if (info == null) {
            return "未注册";
        }
        return info.toString();
    }
    
    /**
     * 内部日志方法
     */
    private void log(String message) {
        if (debugMode) {
            System.out.println("[SimplifiedDisposer] " + message);
        }
    }
    
    /**
     * 内部类：存储 Disposable 的元数据
     */
    private static class DisposableInfo {
        String name;
        Disposable disposable;
        Disposable parent;
        boolean isDisposed = false;
        List<Disposable> children = new CopyOnWriteArrayList<>();
        List<Runnable> disposeCallbacks = new CopyOnWriteArrayList<>();
        
        DisposableInfo(Disposable disposable, Disposable parent, String name) {
            this.disposable = disposable;
            this.parent = parent;
            this.name = name;
        }
        
        void addChild(Disposable child) {
            if (!children.contains(child)) {
                children.add(child);
            }
        }
        
        void addDisposeCallback(Runnable callback) {
            if (callback != null) {
                disposeCallbacks.add(callback);
            }
        }
        
        @Override
        public String toString() {
            return String.format("DisposableInfo{name='%s', disposed=%s, children=%d}", 
                    name, isDisposed, children.size());
        }
    }
    
    /**
     * 默认的 Disposable 实现 - 用于 newDisposable() 创建的对象
     */
    private static class DefaultDisposable implements Disposable {
        @Override
        public void dispose() {
            // 默认实现不需要做任何事情
        }
    }
}