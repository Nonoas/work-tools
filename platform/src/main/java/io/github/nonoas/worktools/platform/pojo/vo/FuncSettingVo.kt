package io.github.nonoas.worktools.platform.pojo.vo

import io.github.nonoas.worktools.platform.ext.Plugin
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.SimpleBooleanProperty

/**
 * @author Nonoas
 * @date 2022/1/25
 */
class FuncSettingVo {
    private val funcCode = SimpleStringProperty()
    private val funcName = SimpleStringProperty()
    private val enableFlag = SimpleBooleanProperty()

    constructor()

    constructor(funcCode: String?, funcName: String?, enableFlag: Boolean) {
        setFuncCode(funcCode)
        setFuncName(funcName)
        setEnableFlag(enableFlag)
    }

    fun getFuncCode(): String = funcCode.get()

    fun funcCodeProperty(): SimpleStringProperty = funcCode

    fun setFuncCode(funcCode: String?) {
        this.funcCode.set(funcCode)
    }

    fun getFuncName(): String? = funcName.get()

    fun funcNameProperty(): SimpleStringProperty {
        return funcName
    }

    fun setFuncName(funcName: String?) {
        this.funcName.set(funcName)
    }

    fun isEnableFlag(): Boolean {
        return enableFlag.get()
    }

    fun enableFlagProperty(): SimpleBooleanProperty {
        return enableFlag
    }

    fun setEnableFlag(enableFlag: Boolean) {
        this.enableFlag.set(enableFlag)
    }

    companion object {
        fun covertFrom(plugin: Plugin) = FuncSettingVo().apply {
            funcCode.set(plugin.id)
            funcName.set(plugin.name)
        }
    }

    override fun toString(): String {
        return "FuncSettingVo(funcCode=$funcCode, funcName=$funcName, enableFlag=$enableFlag)"
    }
}