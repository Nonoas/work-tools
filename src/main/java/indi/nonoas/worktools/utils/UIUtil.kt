package indi.nonoas.worktools.utils

import indi.nonoas.worktools.ui.component.MyAlert
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Bounds
import javafx.scene.Node
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.swing.ImageIcon
import javax.swing.filechooser.FileSystemView
import javafx.scene.image.Image as FXImage


/**
 * @author Nonoas
 * @date 2022/7/16
 */
object UIUtil {
    /**
     * 获取UI节点的在屏幕中的坐标边界
     *
     * @param node 当前节点
     * @return UI节点的在屏幕中的坐标边界
     */
    @JvmStatic
    fun getScreeBounds(node: Node): Bounds {
        val bounds: Bounds = node.getBoundsInLocal()
        return node.localToScreen(bounds)
    }

    fun warn(msg: String?): Optional<ButtonType> {
        return showMyAlter(AlertType.WARNING, msg)
    }

    fun error(msg: String?): Optional<ButtonType> {
        return showMyAlter(AlertType.ERROR, msg)
    }

    private fun showMyAlter(type: AlertType?, msg: String?): Optional<ButtonType> {
        return MyAlert(type, msg).showAndWait()
    }

    private fun convertImageIconToFXImage(icon: ImageIcon): FXImage {
        // 获取 ImageIcon 中的 Image 对象
        val image: Image = icon.image

        // 将 Image 转换为 BufferedImage
        val bufferedImage = BufferedImage(
                image.getWidth(null),
                image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB
        )
        // 绘制 Image 到 BufferedImage 上
        bufferedImage.graphics.drawImage(image, 0, 0, null)

        // 将 BufferedImage 转换为 WritableImage
        val fxImage: FXImage = SwingFXUtils.toFXImage(bufferedImage, null)

        // 返回转换后的 FXImage
        return fxImage
    }

    /**
     * 获取文件的系统图标
     */
    fun getFileIcon(file: File): FXImage? {
        val fsv = FileSystemView.getFileSystemView()
        val icon = fsv.getSystemIcon(file, 32, 32) as ImageIcon?
        val fxImage = icon?.let { convertImageIconToFXImage(it) }
        return fxImage
    }

    fun getFileIcon(path: String): FXImage? {
        return getFileIcon(File(path))
    }
}
