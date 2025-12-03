package indi.nonoas.worktools.windowsutil;


import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.W32APIOptions;

/**
 * @author huangshengsheng
 * @date 2025/12/3 21:55
 */
public class WindowUtils {

    public interface User32 extends com.sun.jna.platform.win32.User32 {
        User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

        HWND GetForegroundWindow();

        boolean SetWindowPos(HWND hWnd, HWND hWndInsertAfter,
                             int X, int Y, int cx, int cy, int uFlags);
    }

    private static final HWND HWND_TOPMOST = new HWND(new Pointer(-1));
    private static final int SWP_NOSIZE = 0x0001;
    private static final int SWP_NOMOVE = 0x0002;
    private static final int SWP_SHOWWINDOW = 0x0040;
    private static final HWND HWND_NOTOPMOST = new HWND(new Pointer(-2));

    // 获取当前选中的（前台）窗口
    public static HWND getForegroundWindow() {
        return User32.INSTANCE.GetForegroundWindow();
    }

    public static String getWindowTitle(HWND hwnd) {
        char[] buffer = new char[512];
        User32.INSTANCE.GetWindowText(hwnd, buffer, buffer.length);
        return Native.toString(buffer);
    }

    // 将窗口置顶
    public static void setTopMost(HWND hwnd) {
        User32.INSTANCE.SetWindowPos(hwnd, HWND_TOPMOST, 0, 0, 0, 0,
                SWP_NOMOVE | SWP_NOSIZE | SWP_SHOWWINDOW);
    }

    // 取消置顶
    public static void removeTopMost(HWND hwnd) {
        User32.INSTANCE.SetWindowPos(hwnd, HWND_NOTOPMOST, 0, 0, 0, 0,
                SWP_NOMOVE | SWP_NOSIZE | SWP_SHOWWINDOW);
    }
}
