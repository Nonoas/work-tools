package io.github.nonoas.worktools.platform.view

import github.nonoas.jfx.flat.ui.control.Card
import github.nonoas.jfx.flat.ui.control.UIFactory
import github.nonoas.jfx.flat.ui.pane.JustifiedFlowPane
import github.nonoas.jfx.flat.ui.theme.Styles
import io.github.nonoas.worktools.platform.common.CommonInsets
import io.github.nonoas.worktools.platform.dao.FuncSettingDao
import io.github.nonoas.worktools.platform.ext.FuncPaneFactory
import io.github.nonoas.worktools.platform.ext.Plugin
import io.github.nonoas.worktools.platform.ext.PluginManager
import io.github.nonoas.worktools.platform.ext.Searchable
import io.github.nonoas.worktools.platform.global.ExtensionManager
import io.github.nonoas.worktools.platform.global.message.MsgBusManager
import io.github.nonoas.worktools.platform.pojo.params.FuncSettingQry
import io.github.nonoas.worktools.platform.pojo.vo.ExecFileVo
import io.github.nonoas.worktools.platform.service.impl.FuncSettingService
import io.github.nonoas.worktools.platform.ui.Reinitializable
import io.github.nonoas.worktools.platform.ui.component.BaseStage
import io.github.nonoas.worktools.platform.ui.component.LlmSearchListener
import io.github.nonoas.worktools.platform.ui.component.SearchListener
import io.github.nonoas.worktools.platform.ui.component.SearchModeTextField
import javafx.collections.ListChangeListener
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.control.Tab
import javafx.scene.control.Tooltip
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.HeaderBar
import javafx.scene.layout.HeaderDragType
import org.apache.logging.log4j.LogManager
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL
import org.kordamp.ikonli.material2.Material2MZ

class MainStage private constructor() : BaseStage(), Reinitializable {

    private val log = LogManager.getLogger(MainStage::class.java)

    private val rootPane = BorderPane()
    private val menuBar = MenuBar()
    private val tfSearch = SearchModeTextField()
    private val llmResultPane = LlmResultPane()

    private val fpFuncList = JustifiedFlowPane(10.0, 10.0, 200.0).apply {
        padding = CommonInsets.PADDING_20
    }

    private val fpFuncListPane = ScrollPane()

    private val funcTabPane = MainFuncPane().apply {
        tabs.addListener(ListChangeListener { change ->
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
    private var funcEnabledMap = LinkedHashMap<String, FuncPaneFactory>()

    /**
     * 当前功能代码索引，当前切换到 funcCodeList 的第几个元素
     */
    private var currFuncIndex = 0

    init {
        initView()
    }

    private fun initView() {
        isAlwaysOnTop = true
        isResizable = true
        minHeight = 400.0
        minWidth = 600.0

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

        headerBar.leading = btnListFunc

        // 搜索框
        initSearchTextField()

        Tooltip.install(
            lbTips, Tooltip(
                """
            快捷键：
            Ctrl+Q  切换界面
            Alt+Shift+M  显示/隐藏窗口
        """.trimIndent()
            )
        )

        val headerCenter = HBox(CommonInsets.SPACING_1, tfSearch, lbTips).apply {
            padding = CommonInsets.PADDING_10
        }
        headerCenter.alignment = Pos.CENTER
        HeaderBar.setDragType(headerCenter, HeaderDragType.DRAGGABLE)
        HeaderBar.setAlignment(headerCenter, Pos.CENTER)
        headerBar.center =headerCenter

        HeaderBar.setMargin(headerBar.leading, CommonInsets.PADDING_L10)
        HeaderBar.setMargin(headerBar.trailing, CommonInsets.PADDING_R10)
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

                override fun onEntered(event: ActionEvent) {
                    resultPane?.onEntered(event)
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
     * 刷新主界面展示的功能卡片。
     *
     * 功能是否展示由 `func_setting.func_code` 控制，编码取自每个功能控制器的
     * `FuncPaneFactory.getCode()`，而不是插件 id。这样可以正确处理一个插件
     * 发布多个功能的情况，例如平台插件同时包含 JDK 版本管理、SQL 转换、待办
     * 和文件编码。每个功能会独立判断启用状态，并使用同一个功能编码注册到
     * `funcEnabledMap`，确保全局搜索和 Tab 路由能打开正确页面。
     */
    private fun refreshFuncPane() {
        fpFuncList.children.clear()
        funcEnabledMap.clear()
        val settingMap = getSettingMap()
        PluginManager.getAll().forEach { plugin ->
            val funcPanes = plugin.getExtensionByType(FuncPaneFactory::class.java)
            funcPanes?.forEach { func ->
                if (!isFuncEnabled(plugin, func, settingMap)) {
                    return@forEach
                }
                val myCard = Card(func.getName(), func.getDescription(), func.getGraphic()).apply {
                    prefWidth = 20.0
                    prefHeight = 90.0
                    onMouseClicked = EventHandler { routeCenter(func) }
                }
                funcEnabledMap[func.getCode()] = func
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
        title = name
    }

    /**
     * 读取持久化的功能可见性配置。
     *
     * 返回结果以功能编码为主，也就是主界面路由和搜索使用的
     * `FuncPaneFactory.getCode()`。旧版本保存的插件 id 记录不会在这里过滤掉，
     * 因为 `isFuncEnabled` 需要用它们为已有用户提供兼容兜底。
     *
     * @return 持久化启用状态，键可能是功能编码，也可能是旧版本插件 id
     */
    private fun getSettingMap(): Map<String, Boolean> {
        return FuncSettingDao()
            .getAll()
            .associate { it.funcCode to it.isEnableFlag }
    }

    /**
     * 判断某个功能是否应该渲染到主界面。
     *
     * 查找顺序与插件设置窗口保持一致：功能级记录优先；功能级记录不存在时，
     * 使用旧版本插件级记录；两者都不存在时，使用插件当前运行态。这样既能让
     * 新版本保存功能级配置，又能兼容已经存在的插件级配置。
     *
     * @param plugin 功能所属插件
     * @param function 用于创建卡片和页面的功能工厂
     * @param settingMap 从 `func_setting` 读取的持久化启用状态
     * @return `true` 表示功能卡片应显示
     */
    private fun isFuncEnabled(
        plugin: Plugin,
        function: FuncPaneFactory,
        settingMap: Map<String, Boolean>
    ): Boolean {
        return settingMap[function.getCode()]
            ?: settingMap[plugin.id]
            ?: plugin.isEnabled
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
