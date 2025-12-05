package indi.nonoas.worktools.platform.global;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 简易版的消息总线实现 (Message Bus).
 * 允许Listener订阅特定的Topic，并通过该Topic的发布者发送消息。
 *
 * @author huangshengsheng
 * @date 2025/12/5 14:50
 */
public class MessageBus {

    // 存储 Topic -> 监听器列表 的映射。使用 ConcurrentHashMap 确保线程安全。
    // Key 是 Topic 对象，Value 是订阅该 Topic 的所有监听器。
    // 为了简化，我们使用 Object 作为 Topic 的Key，实际应用中推荐使用 Class 或 Topic 接口。
    private final Map<Object, Map<Object, Consumer<Object>>> topicListeners = new ConcurrentHashMap<>();

    // ---------------------- 订阅者/监听器 (Subscriber) ----------------------

    /**
     * 连接到消息总线并返回一个连接对象，用于订阅和取消订阅。
     *
     * @return 消息总线连接
     */
    public Connection connect() {
        return new Connection(this);
    }

    // ---------------------- 发布者 (Publisher) ----------------------

    /**
     * 获取指定 Topic 的发布者接口。
     * 发布者用于向所有订阅该 Topic 的监听器发送消息。
     *
     * @param topicKey     用于标识 Topic 的唯一键 (例如: 一个 String 或 Class)
     * @param listenerType 该 Topic 监听器接口的 Class 类型
     * @return 一个 Consumer 接口，调用其 accept 方法即可发布消息
     */
    @SuppressWarnings("unchecked")
    public <L> L getPublisher(Object topicKey, Class<L> listenerType) {
        // 实际的 IntelliJ Message Bus 使用 Proxy 动态生成发布者对象，这里我们使用 Consumer 简化
        Map<Object, Consumer<Object>> listeners = topicListeners.get(topicKey);

        // 返回一个 Lambda/匿名函数作为“发布者”，当调用时遍历并通知所有订阅者
        return (L) new Consumer<Object>() {
            @Override
            public void accept(Object message) {
                if (listeners != null) {
                    // 遍历所有订阅者并调用其 accept 方法处理消息
                    listeners.values().forEach(listener -> {
                        try {
                            // 消息传递，实际中需要通过反射调用监听器接口的方法，这里简化为 Consumer.accept()
                            listener.accept(message);
                        } catch (Exception e) {
                            System.err.println("Error dispatching message for topic " + topicKey + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                }
            }
        };
    }


    // ---------------------- 连接管理 (Connection) ----------------------

    /**
     * 消息总线的连接，用于管理订阅。
     * 实际的 IntelliJ IDEA 连接还包含 Disposer 机制来自动清理资源。
     */
    public static class Connection {
        private final MessageBus bus;
        // 存储本次连接的所有订阅，用于快速取消订阅
        private final Map<Object, Object> activeSubscriptions = new ConcurrentHashMap<>();

        private Connection(MessageBus bus) {
            this.bus = bus;
        }

        /**
         * 订阅一个 Topic。
         *
         * @param topicKey 用于标识 Topic 的唯一键 (例如: 一个 String 或 Class)
         * @param listener 监听器实例，这里简化为处理 Object 消息的 Consumer
         */
        public void subscribe(Object topicKey, Consumer<Object> listener) {
            // 将 Topic Key 映射到监听器集合，如果不存在则创建
            bus.topicListeners.computeIfAbsent(topicKey, k -> new ConcurrentHashMap<>())
                    // 将监听器添加到该 Topic 的集合中
                    .put(listener, listener);

            // 记录此连接的订阅，用于 dispose
            activeSubscriptions.put(topicKey, listener);
        }

        /**
         * 断开连接，移除所有通过此连接注册的监听器。
         */
        public void dispose() {
            for (Map.Entry<Object, Object> entry : activeSubscriptions.entrySet()) {
                Object topicKey = entry.getKey();
                Object listener = entry.getValue();

                Map<Object, Consumer<Object>> listeners = bus.topicListeners.get(topicKey);
                if (listeners != null) {
                    listeners.remove(listener);
                    // 如果该 Topic 下没有监听器了，则清理 Topic
                    if (listeners.isEmpty()) {
                        bus.topicListeners.remove(topicKey);
                    }
                }
            }
            activeSubscriptions.clear();
            System.out.println("Connection disposed. All subscriptions removed.");
        }
    }
}