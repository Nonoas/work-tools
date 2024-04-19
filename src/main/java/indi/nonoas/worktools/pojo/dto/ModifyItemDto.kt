package indi.nonoas.worktools.pojo.dto;

import javafx.beans.property.SimpleBooleanProperty

/**
 * @author Nonoas
 * @date 2022/5/9
 */
class ModifyItemDto {

	private val selected = SimpleBooleanProperty()

	/**
	 * 绝对路径
	 */
	var absolutePath: String? = null

	/**
	 * 修改单号
	 */
	var modifyNum: String? = null

	var timestamp: Long = 0

	/**
	 * 描述
	 */
	var desc: String? = ""


	/**
	 * 修改版本号
	 */
	var versionNO: String? = null


	fun getSelected(): Boolean {
		return selected.get()
	}

	fun selectedProperty(): SimpleBooleanProperty {
		return selected
	}

	fun setSelected(selected: Boolean) {
		this.selected.set(selected)
	}

	override fun toString(): String {
		return "$modifyNum-$desc-$versionNO"
	}
}
