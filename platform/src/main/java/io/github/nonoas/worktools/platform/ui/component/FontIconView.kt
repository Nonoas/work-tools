package io.github.nonoas.worktools.platform.ui.component

import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.javafx.IkonResolver

class FontIconView(ikon: Ikon, size: Int, color: Color) : javafx.scene.text.Text() {
    init {
        val ikonHandler = IkonResolver.getInstance().resolve(ikon.description)
        val font = ikonHandler.font as Font
        val sizedFont = Font(font.family, size.toDouble())

        this.font = sizedFont
        this.fill = color

        val code = ikon.code
        text = if (code <= '\uFFFF'.code) {
            code.toChar().toString()
        } else {
            String(Character.toChars(code))
        }
    }
}