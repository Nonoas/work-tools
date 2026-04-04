package io.github.nonoas.worktools.platform.global.message;

/**
 * 消息总线管理器，存放各个层级的消息总线
 *
 * @author huangshengsheng
 * @date 2026/1/10 11:54
 */
public class MsgBusManager {
    private static final MessageBus globalBus = new MessageBus();

    private static MessageBus currentBus = new MessageBus();

    public static MessageBus getGlobalBus() {
        return globalBus;
    }

    public static MessageBus getCurrentBus() {
        return currentBus;
    }

}