package indi.nonoas.worktools.platform.global;

/**
 * @author huangshengsheng
 * @date 2026/1/9 18:39
 */
public interface Disposable {
    void dispose();

    public interface Parent extends Disposable {
        void beforeTreeDispose();
    }
}
