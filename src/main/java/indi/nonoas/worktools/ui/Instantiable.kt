package indi.nonoas.worktools.ui

/**
 * 一个可以通过 getInstance 获取自身实例的接口
 *
 * @author Nonoas
 * @datetime 2022/5/12 20:40
 */
interface Instantiable {
    val instance: Instantiable?
}
