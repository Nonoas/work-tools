package indi.nonoas.worktools.common

import javafx.geometry.Insets

/**
 * @author Nonoas
 * @date 2021/9/5
 */
interface CommonInsets {
    companion object {
        const val ROOT_PADDING_WIDTH = 20.0

        val ROOT_PANE_PADDING = Insets(20.0, 20.0, 20.0, 20.0)
        val PADDING_10 = Insets(10.0)
        val PADDING_20 = Insets(ROOT_PADDING_WIDTH)
        val PADDING_T20_R20_L20 = Insets(ROOT_PADDING_WIDTH, ROOT_PADDING_WIDTH, 0.0, ROOT_PADDING_WIDTH)
        val PADDING_R10 = Insets(0.0, 10.0, 0.0, 0.0)
        val PADDING_T20 = Insets(20.0, 0.0, 0.0, 0.0)
    }
}