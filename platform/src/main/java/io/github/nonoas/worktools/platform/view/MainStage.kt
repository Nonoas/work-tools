package io.github.nonoas.worktools.platform.view

import github.nonoas.jfx.flat.ui.control.Card
import github.nonoas.jfx.flat.ui.control.UIFactory
import github.nonoas.jfx.flat.ui.pane.JustifiedFlowPane
import github.nonoas.jfx.flat.ui.theme.Styles
import io.github.nonoas.worktools.platform.common.CommonInsets
import io.github.nonoas.worktools.platform.dao.FuncSettingDao
import io.github.nonoas.worktools.platform.ext.FuncPaneFactory
import io.github.nonoas.worktools.platform.ext.PluginManager
import io.github.nonoas.worktools.platform.ext.Searchable
import io.github.nonoas.worktools.platform.global.ExtensionManager
import io.github.nonoas.worktools.platform.global.message.MsgBusManager
import io.github.nonoas.worktools.platform.pojo.dto.FuncSettingDto
import io.github.nonoas.worktools.platform.pojo.params.FuncSettingQry
import io.github.nonoas.worktools.platform.pojo.vo.ExecFileVo
import io.github.nonoas.worktools.platform.service.impl.FuncSettingService
import io.github.nonoas.worktools.platform.ui.Reinitializable
import io.github.nonoas.worktools.platform.ui.component.BaseStage
import io.github.nonoas.worktools.platform.ui.component.LlmSearchListener
import io.github.nonoas.worktools.platform.ui.component.SearchListener
import io.github.nonoas.worktools.platform.ui.component.SearchModeTextField
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.control.Tab
import javafx.scene.control.ToolBar
import javafx.scene.control.Tooltip
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import org.apache.logging.log4j.LogManager
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL
import org.kordamp.ikonli.material2.Material2MZ

class MainStage private constructor() : BaseStage(), Reinitializable {

    private val log = LogManager.getLogger(MainStage::class.java)

    private val rootPane = BorderPane()
    private var toolBar = ToolBar()
    private val menuBar = MenuBar()
    private val tfSearch = SearchModeTextField()
    private val llmResultPane = LlmResultPane()

    private val fpFuncList = JustifiedFlowPane(10.0, 10.0, 200.0).apply {
        padding = CommonInsets.PADDING_20
    }

    private val fpFuncListPane = ScrollPane()

    private val funcTabPane = MainFuncPane().apply {
        tabs.addListener (ListChangeListener {change->
            if (change.list.isEmpty()) {
                rootPane.center = fpFuncListPane
            }
        })
    }

    /**
     * 小提示标签
     */
    private val lbTips = FontIcon(Material2AL.HELP_OUTLINE)

    private val funcService = FuncSettingService()

    /**
     * 功能代码保存
     */
    private var funcEnabledMap = HashMap<String, FuncPaneFactory>()

    /**
     * 当前功能代码索引，当前切换到 funcCodeList 的第几个元素
     */
    private var currFuncIndex = 0

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
                tfSearch.focusInput()
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
            val keys = funcEnabledMap.values.toList()
            if (keys.isNotEmpty()) {
                routeCenter(keys[++currFuncIndex % keys.size])
            }
        }

        fpFuncListPane.apply {
            isFitToWidth = true
            content = fpFuncList
        }
        rootPane.apply {
            top = toolBar
            center = fpFuncListPane
        }
        setContentView(rootPane)
    }

    /**
     * 初始化菜单栏
     */
    private fun initMenuBar() {
        // 设置
        val menuSetting = Menu(null, UIFactory.createMenuButton())

        val itemFunc = MenuItem("插件管理").apply {
            onAction = EventHandler { PluginSettingStage().show() }
        }

        val itemAbout = MenuItem("关于").apply {
            onAction = EventHandler { AboutAlerts.instance?.show() }
        }
        val itemUpgrade = MenuItem("更新").apply {
            onAction = EventHandler { /* todo */ }
        }

        menuSetting.items.addAll(itemFunc, itemUpgrade, itemAbout)

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
            onAction = EventHandler { rootPane.center = fpFuncListPane }
        }

        toolBar.items.add(btnListFunc)

        // 搜索框
        initSearchTextField()
        toolBar.items.add(tfSearch)

        Tooltip.install(
            lbTips, Tooltip(
                """
            快捷键：
            Ctrl+Q  切换界面
            Alt+Shift+M  显示/隐藏窗口
        """.trimIndent()
            )
        )
        toolBar.items.add(lbTips)

        toolBar.items.add(Region().apply { HBox.setHgrow(this, Priority.ALWAYS) })
        toolBar.items.add(HBox().apply { children.addAll(systemButtons) })
        registryDragger(toolBar)
    }

    /**
     * 初始化功能搜索框
     */
    private fun initSearchTextField() {
        MsgBusManager.getGlobalBus().connect()
            .subscribe(SearchListener.TOPIC, object : SearchListener {

                var resultPane: SearchResultPane? = null

                override fun onTextChange(keyword: String) {

                    if (keyword.isBlank()) {
                        rootPane.center = fpFuncListPane
                        return
                    }

                    val qry = FuncSettingQry().apply {
                        funcCode = keyword
                        funcName = keyword
                        enableFlag = true
                        pageSize = 10
                    }

                    val execFileVoList = ArrayList<ExecFileVo>()
                    ExtensionManager.getExtensions(Searchable::class.java).forEach { extension ->
                        val qryResult = extension.onSearchKeywordChange(keyword)
                        execFileVoList.addAll(qryResult)
                    }
                    resultPane = SearchResultPane.Builder()
                        .funcSettings(funcService.search(qry))
                        .execFiles(execFileVoList)
                        .build()

                    rootPane.center = resultPane
                }

                override fun onKeyPressed(event: KeyEvent) {
                    resultPane?.handle(event)
                }

                override fun onEntered(event: KeyEvent) {
                    resultPane?.handle(event)
                }
            })

        MsgBusManager.getGlobalBus().connect()
            .subscribe(LlmSearchListener.TOPIC, object : LlmSearchListener {
                override fun onLoading(prompt: String) {
                    llmResultPane.showLoading(prompt)
                    rootPane.center = llmResultPane
                }

                override fun onResponse(prompt: String, response: String) {
                    llmResultPane.showResponse(prompt, response)
                    rootPane.center = llmResultPane
                }

                override fun onError(prompt: String, message: String) {
                    llmResultPane.showError(prompt, message)
                    rootPane.center = llmResultPane
                }

                override fun onCleared() {
                    rootPane.center = fpFuncListPane
                }
            })
    }

    /**
     * 刷新功能面板
     */
    private fun refreshFuncPane() {
        fpFuncList.children.clear()
        val settingMaps = getSettingMap().filterValues { it.isEnableFlag }
        settingMaps.values.forEach { plugDto ->
            val plugin = PluginManager.getPluginById(plugDto.funcCode) ?: return

            val funcPanes = plugin.getExtensionByType(FuncPaneFactory::class.java)
            funcPanes?.forEach { func ->
                val myCard = Card(func.getName(), func.getDescription(), func.getGraphic()).apply {
                    prefWidth = 20.0
                    prefHeight = 90.0
                    onMouseClicked = EventHandler { routeCenter(func) }
                }
                funcEnabledMap[plugDto.funcCode] = func
                fpFuncList.children.add(myCard)
            }

        }
    }

    fun routeCenter(funcCode: String) {
        val paneFactory = funcEnabledMap[funcCode] ?: return
        routeCenter(paneFactory)
    }

    /**
     * 切换主面板
     */
    private fun routeCenter(factory: FuncPaneFactory) {
        val name = factory.getName()
        val code = factory.getCode()

        rootPane.center = funcTabPane

        var tabCurr: Tab? = null
        for (tab in funcTabPane.tabs) {
            if (tab.userData == code) {
                tabCurr = tab
                break
            }
        }

        if (tabCurr == null) {
            tabCurr = funcTabPane.open(factory)
            tabCurr.userData = code
        }

        funcTabPane.selectionModel.select(tabCurr)
        setTitle(name)
    }

    /**
     * 获取工具栏配置
     * @return K:菜单编码 V:配置数据对象
     */
    private fun getSettingMap(): Map<String, FuncSettingDto> {
        val settingMap = FuncSettingDao()
            .getAll()
            .associateBy { it.funcCode }
            .toMutableMap()
        return settingMap
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
