package indi.yiyi.stockmonitor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import github.nonoas.jfx.flat.ui.stage.ToastQueue
import indi.yiyi.stockmonitor.data.StockGroup
import indi.yiyi.stockmonitor.data.StockRow
import indi.yiyi.stockmonitor.utils.AppConfig
import indi.yiyi.stockmonitor.utils.GroupConfig
import indi.yiyi.stockmonitor.utils.UIUtil
import indi.yiyi.stockmonitor.view.StockColorSettingsDialog
import indi.yiyi.stockmonitor.view.StockSearchDialog
import indi.yiyi.stockmonitor.view.StockTab
import io.github.nonoas.worktools.platform.ext.FuncPane
import io.github.nonoas.worktools.platform.ui.component.FXAlert
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.geometry.Insets
import javafx.geometry.Side
import javafx.scene.Parent
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.jetbrains.annotations.NotNull
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Supplier

class StockMonitorPane : FuncPane() {

    companion object {
        private val LOG = LogManager.getLogger(StockMonitorPane::class.java)
        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        private val HTTP_TIMEOUT: Duration = Duration.ofSeconds(8)
        private const val REFRESH_INTERVAL_SECONDS = 3L
    }

    private val root = BorderPane()
    private val tabPane = TabPane()
    private val groups: MutableMap<String, StockGroup> = ConcurrentHashMap()
    private val marketDict = mapOf(
        "0" to "SZ",
        "1" to "SH"
    )

    /**
     * 防止同一只股票重复并发请求
     * key = groupName_marketCode_stockCode
     */
    private val inFlightRequests: MutableMap<String, Boolean> = ConcurrentHashMap()

    private val http: HttpClient = HttpClient.newBuilder()
        .connectTimeout(HTTP_TIMEOUT)
        .version(HttpClient.Version.HTTP_1_1)
        .build()

    private val mapper = ObjectMapper()
    private val stageSupplier = Supplier { root.scene.window as Stage }
    private val refreshVersion = AtomicLong(0)

    private var scheduler: ScheduledExecutorService? = null
    private var currentRefreshTask: ScheduledFuture<*>? = null
    @Volatile
    private var currentSession: RefreshSession? = null

    private var initialized = false
    @Volatile
    private var disposed = false

    private val tabSelectionListener = ChangeListener<Tab?> { _, _, newValue ->
        if (newValue == null || disposed) {
            return@ChangeListener
        }
        switchToGroup(newValue.text)
    }

    private class RefreshSession(
        val groupName: String,
        val version: Long
    ) {
        @Volatile
        var active: Boolean = true

        fun deactivate() {
            active = false
        }
    }

    @NotNull
    private fun getMenuBarInternal(): MenuBar {
        val addItem = MenuItem("添加股票")
        addItem.setOnAction {
            showAddStockDialog(stageSupplier.get())
        }

        val addGroupItem = MenuItem("添加分组")
        addGroupItem.setOnAction {
            val stage = stageSupplier.get()
            val dialog = TextInputDialog()
            dialog.initOwner(stage)
            dialog.title = "添加分组"
            dialog.headerText = "请输入新分组名称"
            dialog.contentText = "分组名称:"

            dialog.showAndWait().ifPresent { name ->
                val groupName = name?.trim().orEmpty()
                if (groupName.isEmpty()) {
                    FXAlert.info(stage, "输入无效", "分组名称不能为空！")
                    return@ifPresent
                }
                if (!GroupConfig.addGroup(groupName)) {
                    FXAlert.info(stage, "添加失败", "分组【$groupName】已存在！")
                    return@ifPresent
                }
                addGroupTab(groupName)
            }
        }

        val colorSetting = MenuItem("颜色设置")
        colorSetting.setOnAction {
            val dialog = StockColorSettingsDialog()
            dialog.initOwner(stageSupplier.get())
            dialog.showAndWait().ifPresent { newColors ->
                val upColorStyle = UIUtil.toWebColor(newColors.upColor)
                val downColorStyle = UIUtil.toWebColor(newColors.downColor)

                AppConfig.getConfigManager().set("color.up", upColorStyle)
                AppConfig.getConfigManager().set("color.down", downColorStyle)

                applyTabPaneColorStyle(upColorStyle, downColorStyle)
            }
        }

        val menu = Menu("菜单", null, addItem, addGroupItem, colorSetting)
        return MenuBar(menu).apply {
            padding = Insets(5.0, 10.0, 5.0, 10.0)
        }
    }

    private fun applyCurrentColorStyle() {
        applyTabPaneColorStyle(
            AppConfig.getConfigManager().get("color.up", "#e53935"),
            AppConfig.getConfigManager().get("color.down", "#1d9f3e")
        )
    }

    private fun applyTabPaneColorStyle(upColor: String, downColor: String) {
        val mergedStyle = "-stock-up-color: $upColor; -stock-down-color: $downColor;"
        tabPane.style = mergedStyle
    }

    private fun initSchedulerIfNeeded() {
        if (scheduler != null && !scheduler!!.isShutdown) {
            return
        }
        scheduler = Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "stock-refresh-scheduler").apply {
                isDaemon = true
            }
        }
    }

    private fun initGroups() {
        for (g in GroupConfig.getGroups()) {
            addGroupTab(g.name)
        }
        tabPane.selectionModel.selectFirst()
    }

    private fun addGroupTab(groupName: String) {
        val group = StockGroup(groupName)
        groups[groupName] = group

        val tab = StockTab(group).apply {
            isClosable = false
            contextMenu = createGroupContextMenu(this, groupName)
        }

        tabPane.tabs.add(tab)
        tabPane.selectionModel.select(tab)
    }

    private fun createGroupContextMenu(tab: Tab, groupName: String): ContextMenu {
        val contextMenu = ContextMenu()
        val deleteItem = MenuItem("删除分组")

        deleteItem.setOnAction {
            if (tabPane.tabs.size == 1) {
                FXAlert.info(stageSupplier.get(), "删除分组", "无法删除唯一的分组")
                return@setOnAction
            }

            FXAlert.confirm(stageSupplier.get(), "删除分组", "确认删除分组【$groupName】吗？\n删除后无法恢复！")
                .ifPresent { btn ->
                    if (btn != ButtonType.OK) {
                        return@ifPresent
                    }

                    val deletingCurrent = isCurrentTab(groupName)

                    GroupConfig.removeGroup(groupName)
                    groups.remove(groupName)
                    tabPane.tabs.remove(tab)

                    if (deletingCurrent) {
                        stopCurrentRefreshSession()
                        val selected = tabPane.selectionModel.selectedItem
                        if (selected != null) {
                            switchToGroup(selected.text)
                        }
                    }
                }
        }

        contextMenu.items.add(deleteItem)
        return contextMenu
    }

    private fun isCurrentTab(groupName: String): Boolean {
        val selected = tabPane.selectionModel.selectedItem
        return selected != null && selected.text == groupName
    }

    private fun switchToGroup(groupName: String) {
        if (disposed) return

        stopCurrentRefreshSession()
        prepareRowsForGroup(groupName)

        val session = RefreshSession(groupName, refreshVersion.incrementAndGet())
        currentSession = session

        refreshSessionOnce(session)

        val localScheduler = scheduler
        if (localScheduler != null && !localScheduler.isShutdown) {
            currentRefreshTask = localScheduler.scheduleAtFixedRate(
                { refreshSessionOnce(session) },
                REFRESH_INTERVAL_SECONDS,
                REFRESH_INTERVAL_SECONDS,
                TimeUnit.SECONDS
            )
        }
    }

    private fun stopCurrentRefreshSession() {
        val oldSession = currentSession
        currentSession = null

        currentRefreshTask?.cancel(false)
        currentRefreshTask = null

        oldSession?.deactivate()
    }

    /**
     * 首次进入 tab 先铺满占位行
     */
    private fun prepareRowsForGroup(groupName: String) {
        val group = groups[groupName] ?: return
        val stocks = GroupConfig.getStocksOf(groupName)

        Platform.runLater {
            if (disposed) return@runLater

            val table = group.tableView
            table.items.clear()
            table.rowByKey.clear()

            var index = 1
            for (stock in stocks) {
                val row = createPlaceholderRow(index++, stock.marketCode(), stock.stockCode())
                val key = buildRowKey(stock.marketCode(), stock.stockCode())
                table.rowByKey[key] = row
                table.items.add(row)
            }
        }
    }

    private fun createPlaceholderRow(index: Int, marketCode: String, stockCode: String): StockRow {
        val codeShown = marketDict[marketCode].orEmpty() + stockCode
        return StockRow(
            index,
            marketCode,
            stockCode,
            codeShown,
            "",
            "--:--:--",
            0.0,
            0.0,
            "--",
            0.0
        )
    }

    /**
     * 每只股票独立刷新
     */
    private fun refreshSessionOnce(session: RefreshSession) {
        if (!isSessionValid(session)) return

        val stocks = GroupConfig.getStocksOf(session.groupName)
        for (stock in stocks) {
            val requestKey = "${session.groupName}_${stock.marketCode()}_${stock.stockCode()}"
            if (inFlightRequests.putIfAbsent(requestKey, true) != null) {
                continue
            }

            fetchStockRowAsync(stock.marketCode(), stock.stockCode())
                .whenComplete { opt, ex ->
                    inFlightRequests.remove(requestKey)

                    if (ex != null) {
                        val msg = ex.cause?.message ?: ex.message
                        LOG.debug("request stock failed: {}.{} -> {}", stock.marketCode(), stock.stockCode(), msg)
                        return@whenComplete
                    }

                    if (opt == null || opt.isEmpty) {
                        return@whenComplete
                    }

                    val latestRow = opt.get()
                    Platform.runLater {
                        applyRowUpdateIfSessionValid(session, latestRow)
                    }
                }
        }
    }

    private fun isSessionValid(session: RefreshSession?): Boolean {
        if (disposed || session == null || !session.active) {
            return false
        }

        val current = currentSession
        if (current == null || current.version != session.version) {
            return false
        }

        val selected = tabPane.selectionModel.selectedItem
        return selected != null && selected.text == session.groupName
    }

    private fun applyRowUpdateIfSessionValid(session: RefreshSession, latestRow: StockRow) {
        if (!isSessionValid(session)) return

        val group = groups[session.groupName] ?: return
        val table = group.tableView
        val key = buildRowKey(latestRow.marketCode, latestRow.rawCode)
        val existed = table.rowByKey[key] ?: return

        existed.name = latestRow.name
        existed.lastUpdateTime = latestRow.lastUpdateTime
        existed.price = latestRow.price
        existed.changeRate = latestRow.changeRate
        existed.changeRateStr = latestRow.changeRateStr
        existed.changeAmt = latestRow.changeAmt
    }

    private fun buildRowKey(marketCode: String, stockCode: String): String {
        return "${marketCode}_${stockCode}"
    }

    private fun showAddStockDialog(owner: Stage) {
        val dialog = StockSearchDialog()
        dialog.initOwner(owner)

        dialog.showAndWait().ifPresent { suggestion ->
            var codeVar = suggestion.code
            val marketVar: String

            when {
                codeVar.startsWith("SZ") -> {
                    marketVar = "0"
                    codeVar = codeVar.replace("SZ", "")
                }
                codeVar.startsWith("SH") -> {
                    marketVar = "1"
                    codeVar = codeVar.replace("SH", "")
                }
                else -> {
                    FXAlert.info(owner, "股票不支持", "暂不支持添加此类型")
                    return@ifPresent
                }
            }

            val market = marketVar
            val code = codeVar

            if (!GroupConfig.addStock(currGroup.name, market, code)) {
                Alert(Alert.AlertType.INFORMATION, "这只股票已经在列表里啦～").showAndWait()
                return@ifPresent
            }

            val waiting = Alert(Alert.AlertType.INFORMATION, "正在校验这只股票是否存在，请稍候…")
            waiting.headerText = null
            waiting.initOwner(owner)
            UIUtil.setDialogIcon(waiting, owner)

            val okBtn = waiting.dialogPane.lookupButton(ButtonType.OK)
            okBtn.isDisable = true
            waiting.show()

            validateStock(market, code).whenComplete { opt, err ->
                Platform.runLater {
                    okBtn.isDisable = false

                    if (err != null || opt == null || opt.isEmpty || StringUtils.isBlank(opt.get().name)) {
                        waiting.contentText = "抱歉，没有找到$code 这只股票的有效行情（可能代码错误/无数据/停牌）。"
                        return@runLater
                    }

                    val row = opt.get()
                    val table = currGroup.tableView
                    val key = buildRowKey(row.marketCode, row.rawCode)

                    synchronized(table) {
                        val existed = table.rowByKey[key]
                        if (existed == null) {
                            row.index = table.items.size + 1
                            table.rowByKey[key] = row
                            table.items.add(row)
                        } else {
                            existed.name = row.name
                            existed.lastUpdateTime = row.lastUpdateTime
                            existed.price = row.price
                            existed.changeRate = row.changeRate
                            existed.changeRateStr = row.changeRateStr
                            existed.changeAmt = row.changeAmt
                        }
                    }

                    waiting.close()
                    ToastQueue.show(
                        stageSupplier.get(),
                        "添加成功：" + if (market == "0") "SZ$code · ${row.name}" else "SH$code · ${row.name}",
                        2000
                    )

                    val session = currentSession
                    if (session != null && isCurrentTab(currGroup.name)) {
                        refreshSessionOnce(session)
                    }
                }
            }
        }
    }

    private val currGroup: StockGroup
        get() = (tabPane.selectionModel.selectedItem as StockTab).stockGroup

    private fun validateStock(market: String, code: String): CompletableFuture<Optional<StockRow>> {
        return fetchStockRowAsync(market, code)
    }

    /**
     * 单只股票异步请求，失败后轻量重试一次
     */
    private fun fetchStockRowAsync(marketCode: String, stockCode: String): CompletableFuture<Optional<StockRow>> {
        return fetchStockRowAsync(marketCode, stockCode, 1)
    }

    private fun fetchStockRowAsync(
        marketCode: String,
        stockCode: String,
        retryCount: Int
    ): CompletableFuture<Optional<StockRow>> {
        val request = buildQuoteRequest(marketCode, stockCode)

        return http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply { response ->
                parseStockResponse(response, marketCode, stockCode)
            }
            .handle { result, ex ->
                if (ex == null) {
                    CompletableFuture.completedFuture(result)
                } else {
                    if (retryCount > 0) {
                        CompletableFuture.supplyAsync(
                            { null },
                            CompletableFuture.delayedExecutor(300, TimeUnit.MILLISECONDS)
                        ).thenCompose {
                            fetchStockRowAsync(marketCode, stockCode, retryCount - 1)
                        }
                    } else {
                        CompletableFuture.completedFuture(Optional.empty())
                    }
                }
            }
            .thenCompose { it }
    }

    private fun buildQuoteRequest(marketCode: String, stockCode: String): HttpRequest {
        val url = "https://push2.eastmoney.com/api/qt/stock/trends2/get?secid=" +
                "$marketCode.$stockCode" +
                "&fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13" +
                "&fields2=f51,f52,f53,f54,f55,f56,f57,f58"

        return HttpRequest.newBuilder(URI.create(url))
            .timeout(HTTP_TIMEOUT)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36")
            .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
            .GET()
            .build()
    }

    private fun parseStockResponse(
        response: HttpResponse<String>?,
        marketCode: String,
        stockCode: String
    ): Optional<StockRow> {
        try {
            if (response == null || response.statusCode() != 200) {
                return Optional.empty()
            }

            val rootNode: JsonNode = mapper.readTree(response.body())
            val dataNode = rootNode.path("data")
            if (dataNode.isMissingNode) {
                return Optional.empty()
            }

            val name = dataNode.path("name").asText("")
            val preClose = dataNode.path("preClose").asDouble()

            val trends = dataNode.path("trends")
            if (trends.isMissingNode || !trends.isArray || trends.isEmpty) {
                return Optional.empty()
            }

            val latestTrend = trends[trends.size() - 1].asText()
            val arr = latestTrend.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (arr.size < 3) {
                return Optional.empty()
            }

            val currentPrice = arr[2].toDouble()
            val changeRate = if (preClose == 0.0) 0.0 else (currentPrice - preClose) / preClose
            val changeAmount = currentPrice - preClose

            val codeShown = marketDict[marketCode].orEmpty() + stockCode
            val changeRateStr = String.format(Locale.CHINA, "%.2f%%", changeRate * 100)
            val lastUpdateTime = LocalDateTime.now().format(TIME_FORMATTER)

            val row = StockRow(
                0,
                marketCode,
                stockCode,
                codeShown,
                name,
                lastUpdateTime,
                currentPrice,
                changeRate,
                changeRateStr,
                changeAmount
            )
            return Optional.of(row)
        } catch (e: IOException) {
            LOG.debug("parse stock response io error: {}.{}", marketCode, stockCode)
            return Optional.empty()
        } catch (e: Exception) {
            LOG.error("parse stock response error: {}.{}", marketCode, stockCode, e)
            return Optional.empty()
        }
    }

    @NotNull
    override fun getRootView(): Parent {
        if (initialized) {
            return root
        }

        disposed = false
        initialized = true

        initSchedulerIfNeeded()

        root.stylesheets.add(javaClass.getResource("/css/style.css")!!.toExternalForm())

        val menuBar = getMenuBarInternal()
        tabPane.side = Side.BOTTOM
        tabPane.selectionModel.selectedItemProperty().addListener(tabSelectionListener)

        applyCurrentColorStyle()
        initGroups()

        root.top = menuBar
        root.center = tabPane

        val selected = tabPane.selectionModel.selectedItem
        if (selected != null) {
            switchToGroup(selected.text)
        }

        return root
    }

    override fun dispose() {
        disposed = true

        stopCurrentRefreshSession()

        scheduler?.shutdownNow()
        scheduler = null

        inFlightRequests.clear()

        tabPane.selectionModel.selectedItemProperty().removeListener(tabSelectionListener)
        tabPane.tabs.clear()
        groups.clear()
        root.center = null
        root.top = null

        initialized = false
    }
}