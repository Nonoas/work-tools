package indi.nonoas.worktools.windowsutil;

import com.sun.jna.platform.win32.WinDef.HWND;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.ImageView;

/**
 * @author huangshengsheng
 * @date 2025/12/3 22:28
 */
public class WindowInfo {
    private final HWND hwnd;
    private final SimpleStringProperty title = new SimpleStringProperty("");
    private final SimpleBooleanProperty topMost = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty<ImageView> icon = new SimpleObjectProperty<>();


    public WindowInfo(HWND hwnd, String title,ImageView icon) {
        this.hwnd = hwnd;
        this.title.set(title);
        this.icon.set(icon);
    }

    public HWND getHwnd() {
        return hwnd;
    }

    public String getTitle() {
        return title.get();
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    public boolean isTopMost() {
        return topMost.get();
    }

    public SimpleBooleanProperty topMostProperty() {
        return topMost;
    }

    public ImageView getIcon() {
        return icon.get();
    }

    public SimpleObjectProperty<ImageView> iconProperty() {
        return icon;
    }

    public void setIcon(ImageView icon) {
        this.icon.set(icon);
    }

}
