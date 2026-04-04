package io.github.nonoas.worktools.platform.global;

/**
 * Disposable 接口 - 标记对象为可释放的资源
 * <p>
 * 任何需要被 Disposer 管理的对象都应该实现此接口
 * 并在 dispose() 方法中清理所有持有的资源
 *
 * @author Nonoas
 */
public interface Disposable {

    /**
     * 释放对象持有的所有资源
     * <p>
     * 实现此方法时需要注意：
     * 1. 应该是幂等的（可以安全地多次调用）
     * 2. 不应该抛出异常
     * 3. 不应该执行长时间的阻塞操作
     */
    void dispose();
}