package indi.nonoas.worktools.platform.global.message;

import cn.hutool.core.collection.CollectionUtil;
import indi.nonoas.worktools.platform.global.Disposable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 简易版的消息总线实现 (Message Bus).
 * 允许Listener订阅特定的Topic，并通过该Topic的发布者发送消息。
 *
 * @author huangshengsheng
 * @date 2025/12/5 14:50
 */
public class MessageBus {

    private final Map<Topic, List<Object>> topicListeners = new ConcurrentHashMap<>();

    private final Map<Topic, Object> publisherCache = new ConcurrentHashMap<>();

    // ---------------------- 发布者 (Publisher) ----------------------

    /**
     * 获取指定 Topic 的发布者接口。
     * 发布者用于向所有订阅该 Topic 的监听器发送消息。
     *
     * @param topicKey 用于标识 Topic 的唯一键 (例如: 一个 String 或 Class)
     * @return 一个 Consumer 接口，调用其 accept 方法即可发布消息
     */
    @SuppressWarnings("unchecked")
    public <L> L getPublisher(Topic<L> topicKey) {
        Class<L> keyInterface = topicKey.getInterface();
        if (!keyInterface.isInterface()) {
            throw new IllegalArgumentException("The given class is not an interface");
        }
        return (L) publisherCache.computeIfAbsent(topicKey,
                topic -> Proxy.newProxyInstance(
                        keyInterface.getClassLoader(),
                        new Class<?>[]{keyInterface},
                        createPublisher(topicKey))
        );
    }

    @NotNull
    private <L> MessagePublisher createPublisher(Topic<L> topicKey) {
        return new MessagePublisher(topicKey, this);
    }

    public Connection connect() {
        return new Connection(this);
    }

    static class MessagePublisher implements InvocationHandler {

        private final Topic<?> topic;
        private final MessageBus messageBus;

        public MessagePublisher(Topic<?> topic, MessageBus messageBus) {
            this.topic = topic;
            this.messageBus = messageBus;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            List<?> objects = messageBus.topicListeners.get(topic);
            if (CollectionUtil.isEmpty(objects)) {
                return null;
            }
            for (Object object : objects) {
                method.invoke(object, args);
            }
            return null;
        }
    }


    // ---------------------- 连接管理 (Connection) ----------------------

    /**
     * 消息总线的连接，用于管理订阅。
     * 实际的 IntelliJ IDEA 连接还包含 Disposer 机制来自动清理资源。
     */
    public static class Connection implements Disposable {
        // 存储本次连接的所有订阅，用于快速取消订阅
        private final Map<Topic, List<Object>> activeSubscriptions = new ConcurrentHashMap<>();
        private final MessageBus bus;

        private Connection(MessageBus messageBus) {
            this.bus = messageBus;
        }

        /**
         * 订阅一个 Topic。
         *
         * @param topicKey 用于标识 Topic 的唯一键 (例如: 一个 String 或 Class)
         * @param listener 监听器实例，这里简化为处理 Object 消息的 Consumer
         */
        public <T> void subscribe(Topic<T> topicKey, T listener) {
            // 将 Topic Key 映射到监听器集合，如果不存在则创建
            bus.topicListeners.computeIfAbsent(topicKey, k -> new CopyOnWriteArrayList<>())
                    // 将监听器添加到该 Topic 的集合中ww
                    .add(listener);
            // 记录此连接的订阅，用于 dispose
            activeSubscriptions.computeIfAbsent(topicKey, k -> new CopyOnWriteArrayList<>())
                    .add(listener);
        }

        /**
         * 断开连接，移除所有通过此连接注册的监听器。
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public void dispose() {
            for (Map.Entry<Topic, List<Object>> entry : activeSubscriptions.entrySet()) {
                Object topicKey = entry.getKey();
                List<Object> listener = entry.getValue();

                List listeners = bus.topicListeners.get(topicKey);
                if (listeners != null) {
                    listeners.removeAll(listener);
                    // 如果该 Topic 下没有监听器了，则清理 Topic
                    if (listeners.isEmpty()) {
                        bus.topicListeners.remove(topicKey);
                    }
                }
                listener.clear();
            }
            activeSubscriptions.clear();
        }
    }
}