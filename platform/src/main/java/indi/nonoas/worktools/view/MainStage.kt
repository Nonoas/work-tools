package indi.nonoas.worktools.view

import atlantafx.base.controls.CustomTextField
import cn.hutool.core.collection.CollectionUtil
import cn.hutool.core.util.StrUtil
import github.nonoas.jfx.flat.ui.control.UIFactory
import github.nonoas.jfx.flat.ui.theme.Styles
import github.nonoas.jfx.flat.ui.theme.Styles.TEXT_MUTED
import github.nonoas.jfx.flat.ui.theme.Styles.TEXT_SMALL
import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.dao.ExecFileDao
import indi.nonoas.worktools.dao.FuncSettingDao
import indi.nonoas.worktools.ext.PluginLoader
import indi.nonoas.worktools.global.FuncManager
import indi.nonoas.worktools.pojo.dto.FuncSettingDto
import indi.nonoas.worktools.pojo.params.FuncSettingQry
import indi.nonoas.worktools.service.impl.FuncSettingService
import indi.nonoas.worktools.ui.Reinitializable
import indi.nonoas.worktools.ui.component.BaseStage
import indi.nonoas.worktools.utils.DBUtil
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.TextField
import javafx.scene.control.ToolBar
import javafx.scene.control.Tooltip
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.FlowPane
import org.apache.logging.log4j.LogManager
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL
import org.kordamp.ikonli.material2.Material2MZ

class MainStage private constructor() : BaseStage(), Reinitializable {

    private val log = LogManager.getLogger(MainStage::class.java)

    private val rootPane = BorderPane()
    private var toolBar = ToolBar()
    private val menuBar = MenuBar()
    private val tfSearch = CustomTextField().apply {
        promptText = "输入关键字，回车搜索"
    }

    private val fpFuncList = FlowPane(10.0, 10.0).apply { padding = CommonInsets.PADDING_20 }

    /**
     * 小提示标签
     */
    private val lbTips = FontIcon(Material2AL.HELP_OUTLINE)

    private val funcService = FuncSettingService()

    /**
     * 功能代码保存
     */
    private var funcEnabledMap = emptyMap<String, FuncSettingDto>()

    /**
     * 当前功能代码索引，当前切换到 funcCodeList 的第几个元素
     */
    private var currFuncIndex = 0

    private var tfSearchEventHandler: EventHandler<KeyEvent>? = null

    init {
        initView()
    }

    private fun initView() {
        setAlwaysOnTop(true)
        setResizable(true)
        setMinHeight(400)
        setMinWidth(600)

        // 监听宽高的变化，保存到静态变量
        stage.widthProperty().addListener { _, _, newValue ->
            width = newValue.toDouble()
        }
        stage.heightProperty().addListener { _, _, newValue ->
            height = newValue.toDouble()
        }

        // 监听窗口显示
        stage.showingProperty().addListener { _, _, newValue ->
            if (newValue) {
                tfSearch.requestFocus()
            }
        }

        stage.apply {
            isAlwaysOnTop = true
            height = 500.0
            width = 600.0
        }

        // 菜单栏
        initMenuBar()
        // 工具栏
        initToolBar()
        // 功能按钮
        refreshFuncPane()

        initScene()
    }

    private fun initScene() {
        val kcToggleFunc = KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN)
        stage.scene.accelerators[kcToggleFunc] = Runnable {
            val keys = funcEnabledMap.keys.toList()
            if (keys.isNotEmpty()) {
                routeCenter(keys[++currFuncIndex % keys.size])
            }
        }

        rootPane.apply {
            top = toolBar
            center = fpFuncList
        }
        setContentView(rootPane)
    }

    /**
     * 初始化菜单栏
     */
    private fun initMenuBar() {
        // 设置
        val menuSetting = Menu(null, UIFactory.createMenuButton())

        val itemFunc = MenuItem("功能").apply {
            onAction = EventHandler { FunctionSettingStage().show() }
        }

        // 插件
        val menuPlugin = Menu("插件")
        PluginLoader.load().forEach { plugin ->
            val item = MenuItem(plugin.name)
            item.onAction = EventHandler { plugin.service.service() }
            menuPlugin.items.add(item)
        }

        val itemAbout = MenuItem("关于").apply {
            onAction = EventHandler { AboutAlerts.instance?.show() }
        }
        val itemUpgrade = MenuItem("更新").apply {
            onAction = EventHandler { /* todo */ }
        }

        menuSetting.items.addAll(itemFunc, menuPlugin, itemUpgrade, itemAbout)

        menuBar.menus.add(menuSetting)
        menuBar.isFocusTraversable = false
        menuBar.styleClass.add("svg-button")

        val pinButton = UIFactory.createPinButton(stage).also {
            Tooltip.install(it, Tooltip("窗口置顶"))
        }

        systemButtons.addAll(0, listOf(pinButton, menuBar))
    }

    /**
     * 初始化工具栏
     */
    private fun initToolBar() {
        val btnListFunc = Button(null, FontIcon(Material2MZ.MENU)).apply {
            styleClass.add(Styles.BUTTON_ICON)
            onAction = EventHandler { rootPane.center = fpFuncList }
        }

        toolBar.items.add(btnListFunc)

        // 搜索框
        initSearchTextField()
        toolBar.items.add(tfSearch)

        Tooltip.install(lbTips, Tooltip("""
            快捷键：
            Ctrl+Q  切换界面
            Alt+Shift+M  显示/隐藏窗口
        """.trimIndent()))
        toolBar.items.add(lbTips)
        registryDragger(toolBar)
    }

    /**
     * 初始化功能搜索框
     */
    private fun initSearchTextField() {
        tfSearch.left = Label("Qry_>").apply {
            styleClass.addAll("hint", TEXT_MUTED, TEXT_SMALL)
            tooltip = Tooltip("搜索模式")
        }

        tfSearch.onTextChanged { query ->
            tfSearchEventHandler?.let {
                tfSearch.removeEventHandler(KeyEvent.KEY_PRESSED, it)
            }

            if (query.isNullOrBlank()) {
                rootPane.center = fpFuncList
                return@onTextChanged
            }

            val qry = FuncSettingQry().apply {
                funcCode = query
                funcName = query
                enableFlag = true
                pageSize = 10
            }

            val resultPane = SearchResultPane.Builder()
                .funcSettings(funcService.search(qry))
                .execFiles(ExecFileDao.search(query))
                .build()

            rootPane.center = resultPane
            tfSearchEventHandler = resultPane
            tfSearch.addEventHandler(KeyEvent.KEY_PRESSED, resultPane)
        }
    }

    private fun refreshFuncPane() {
        fpFuncList.children.clear()
        funcEnabledMap = getSettingMap().filterValues { it.isEnableFlag }

        funcEnabledMap.values.forEach { func ->
            val btnFunc = Button(func.funcName).apply {
                prefWidth = 90.0
                prefHeight = 90.0
                onAction = EventHandler { routeCenter(func.funcCode) }
            }
            fpFuncList.children.add(btnFunc)
        }
    }

    /**
     * 切换主面板
     */
    fun routeCenter(funcCode: String) {
        val rootView = FuncManager.getRootView(funcCode)
        if (rootView == null) {
            log.error("没有找到功能号$funcCode 对应的面板")
            return
        }
        rootPane.center = rootView
        setTitle("$TITLE-${funcEnabledMap[funcCode]?.funcName}")
    }

    /**
     * 获取工具栏配置
     * @return K:菜单编码 V:配置数据对象
     */
    private fun getSettingMap(): Map<String, FuncSettingDto> {
        return FuncSettingDao()
            .getAll()
            .associateBy { it.funcCode }
    }

    /**
     * 文本变化监听器
     */
    private fun TextField.onTextChanged(action: (String?) -> Unit) {
        textProperty().addListener { _, _, newValue -> action(newValue) }
    }

    /**
     * 使窗口显示出来，并显示为上一次窗口隐藏时的大小
     */
    override fun display() {
        stage.width = width
        stage.height = height
        super.display()
    }

    override fun reInit() {
        refreshFuncPane()
    }

    companion object {
        private var width: Double = 0.0
        private var height: Double = 0.0

        @Volatile
        var instance: MainStage? = null
            get() {
                if (field != null) return field
                synchronized(MainStage::class.java) {
                    if (field == null) {
                        field = MainStage()
                    }
                }
                return field
            }
            private set
    }
}
