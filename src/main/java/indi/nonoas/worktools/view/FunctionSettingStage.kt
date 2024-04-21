package indi.nonoas.worktools.view

import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.ui.UIFactory
import indi.nonoas.worktools.dao.FuncSettingDao
import indi.nonoas.worktools.pojo.vo.FuncSettingVo
import indi.nonoas.worktools.ui.TaskHandler
import indi.nonoas.worktools.ui.component.BaseStage
import indi.nonoas.worktools.utils.BeanUtil
import indi.nonoas.worktools.utils.DBUtil
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ToggleButton
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.stage.Modality
import java.util.*
import java.util.stream.Collectors

/**
 * 功能入口：设置-功能
 *
 * @author Nonoas
 * @datetime 2022/1/22 22:33
 */
class FunctionSettingStage : BaseStage() {

    private var vos: List<FuncSettingVo>
    private val toggles: Array<ToggleButton?>


    private fun initView() {
        val gp = GridPane().apply {
            vgap = 10.0
            padding = Insets(40.0, 20.0, 20.0, 20.0)
        }

        val gpCol1 = ColumnConstraints()
        gpCol1.percentWidth = 50.0

        val gpCol2 = ColumnConstraints().apply {
            percentWidth = 50.0
            halignment = HPos.RIGHT
        }

        gp.columnConstraints.addAll(gpCol1, gpCol2)
        var i = 0
        while (i < vos.size) {
            gp.addRow(i, Label(vos[i].getFuncName()), toggles[i])
            i++
        }

        val btnApply = UIFactory.getPrimaryButton("应用")
        btnApply.onAction = EventHandler { onApply() }

        val btnCancel = Button("取消")
        btnCancel.onAction = EventHandler { onCancel() }

        val hBox = HBox(10.0, btnApply, btnCancel)
                .apply {
                    padding = CommonInsets.PADDING_T20
                    alignment = Pos.CENTER_RIGHT
                }

        gp.add(hBox, 1, i)
        setContentView(gp)
    }

    /**
     * 点击 “应用按钮之后触发”
     */
    private fun onApply() {
        TaskHandler<Int?>()
                .whenCall {
                    var result = 0
                    val conn = DBUtil.getConnection()
                    conn.autoCommit = false
                    val dao = FuncSettingDao(conn)
                    result += dao.deleteAll()
                    result += Arrays.stream(dao.insertBatch(vos)).sum()
                    conn.commit()
                    if (result != 0) result else null
                }
                .andThen {
                    this.close()
                    MainStage.instance?.reInit()
                }
                .handle()
    }

    private fun onCancel() {
        close()
    }

    init {
        val settingList = FuncSettingDao(DBUtil.getConnection()).getAll()

        vos = settingList.stream()
                .map { dto -> BeanUtil.map(dto, FuncSettingVo::class.java) }
                .collect(Collectors.toList())

        toggles = arrayOfNulls(vos.size)

        for (i in toggles.indices) {
            toggles[i] = UIFactory.getBaseToggleButton()
            toggles[i]!!.selectedProperty().bindBidirectional(vos[i].enableFlagProperty())
        }
    }

    init {
        setTitle("功能设置")
        stage.apply {
            isResizable = false
            isAlwaysOnTop = true
            width = 300.0
            initModality(Modality.APPLICATION_MODAL)
        }
        initView()
    }
}