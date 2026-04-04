package io.github.nonoas.worktools.platform.global.message;

/**
 * @author huangshengsheng
 * @date 2025/12/27 17:57
 */
public class Topic<T> {

    private final String topicName;
    private final Class<T> tClass;

    public Topic(String topicName, Class<T> tClass) {
        this.topicName = topicName;
        this.tClass = tClass;
    }

    public Class<T> getInterface() {
        return tClass;
    }

    @Override
    public String toString() {
        return "Topic{" + "topicName=" + topicName + ", tClass=" + tClass + '}';
    }

    public static <T> Topic<T> create(String topicName, Class<T> tClass) {
        return new Topic<>(topicName, tClass);
    }


}