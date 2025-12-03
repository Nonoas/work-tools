package indi.nonoas.worktools.windowsutil;

import com.sun.jna.platform.win32.WinDef.HWND;

/**
 * @author huangshengsheng
 * @date 2025/12/3 22:28
 */
public class WindowInfo {
    private final HWND hwnd;
    private final String title;

    public WindowInfo(HWND hwnd, String title) {
        this.hwnd = hwnd;
        this.title = title;
    }

    public HWND getHwnd() {
        return hwnd;
    }

    public String getTitle() {
        return title;
    }
}
