package indi.nonoas.worktools.common

import javafx.geometry.Insets

/**
 * @author Nonoas
 * @date 2021/9/5
 */
interface CommonInsets {
    companion object {
        val PADDING_10 = Insets(10.0)
        val PADDING_20 = Insets(20.0)
        val PADDING_T20_R20_L20 = Insets(20.0, 20.0, 0.0, 20.0)
        val PADDING_R10 = Insets(0.0, 10.0, 0.0, 0.0)
        val PADDING_T20 = Insets(20.0, 0.0, 0.0, 0.0)
    }
}