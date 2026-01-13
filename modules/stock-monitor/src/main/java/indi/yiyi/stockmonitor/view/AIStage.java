package indi.yiyi.stockmonitor.view;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import indi.nonoas.worktools.platform.ui.component.BaseStage;
import indi.yiyi.stockmonitor.data.Stock;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;


/**
 * AIStage: 使用 LangChain4j AiServices，改为聊天气泡布局。
 */
public class AIStage extends BaseStage {

    private static final Logger LOGGER = (Logger) LogManager.getLogger(AIStage.class);

    // UI 组件
    // private final TextArea chatHistoryArea; // 移除 TextArea
    private final VBox chatContainer; // ** 新增：消息气泡容器 **
    private final ScrollPane scrollPane; // ** 新增：滚动面板 **
    private final TextField inputField;
    private final Button sendButton;

    private final Assistant assistant;

    private final List<ToolSpecification> toolSpecifications;

    public AIStage(String deepseekApiKey, List<Stock> stocksOfCurrentGroup) {
        if (deepseekApiKey == null || deepseekApiKey.isEmpty()) {
            throw new IllegalArgumentException("DEEPSEEK_API_KEY 不能为空");
        }

        // 1. 初始化模型
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://api.moonshot.cn/v1")
                .apiKey(deepseekApiKey)
                .modelName("kimi-k2-turbo-preview")
                .build();

        ToolSpecification webSearchSpec = ToolSpecification.builder()
                .name("$web_search") // 必须是 "$web_search"
                .description("这是一个联网搜索工具，当用户提出联网搜索时必须使用这个工具")
                .build();

        toolSpecifications = ToolSpecifications.toolSpecificationsFrom(SearchTools.class);

        String systemMsg = injectDynamicContext(stocksOfCurrentGroup);

        this.assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .systemMessageProvider(memoryId -> systemMsg)
                .tools(Arrays.asList(new SearchTools(), webSearchSpec))
                .build();

        this.chatContainer = new VBox(5); // 间距 5
        this.chatContainer.setAlignment(Pos.TOP_LEFT);
        this.chatContainer.setPadding(new Insets(10, 0, 0, 0));

        this.scrollPane = new ScrollPane(chatContainer);
        this.scrollPane.setFitToWidth(true); // 让 VBox 宽度适配 ScrollPane
        this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // 隐藏水平滚动条
        this.scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        this.inputField = new TextField();
        this.inputField.setPromptText("输入你的消息...");

        this.sendButton = new Button("发送");
        this.sendButton.setOnAction(e -> sendMessage());
        this.inputField.setOnAction(e -> sendMessage());

        // 6. 布局
        HBox inputContainer = new HBox(5, inputField, sendButton);
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputContainer.setPadding(new Insets(10, 0, 0, 0));

        Region header = new Region();
        header.setPrefHeight(30);

        registryDragger(header);

        BorderPane root = new BorderPane();
        root.setCenter(scrollPane); // ** 设置 ScrollPane 为中心 **
        root.setTop(header);
        root.setBottom(inputContainer);
        root.setPadding(new Insets(10));

        // 7. Stage 配置
        setTitle("StockMonitor 聊天助手");
        setContentView(root);

        chatContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            // 确保在 JavaFX UI 线程中执行滚动操作
            Platform.runLater(() -> {
                // 设置垂直滚动值到最大 (1.0)
                scrollPane.setVvalue(1.0);
            });
        });

        // 8. 启动时提示上下文已加载
        appendMessage("欢迎使用 StockMonitor 股票助理。\n我目前的功能还不能获取到最新的行情信息哦，不过我可以跟你聊天呀。", false);
    }

    // ** 新增：统一的消息添加方法 **
    private void appendMessage(String text, boolean isUser) {
        // 使用 Platform.runLater 确保在 JavaFX 线程中更新 UI
        Platform.runLater(() -> {
            MessageBubble bubble = new MessageBubble(text, isUser);
            chatContainer.getChildren().add(bubble);
        });
    }

    private String injectDynamicContext(List<Stock> stocksOfCurrentGroup) {
        return readTextFromProjectRelativePath("config/context");

    }

    /**
     * 从项目启动时的当前工作目录下的指定路径读取文本内容。
     * <p>
     * 路径示例：项目根目录/config/context
     *
     * @param relativePath 相对于项目同级目录的路径，例如："config/context"
     * @return 文件的全部文本内容
     */
    public String readTextFromProjectRelativePath(String relativePath) {

        // 1. 获取当前工作目录 (Current Working Directory, CWD)
        // CWD 是 Java 应用程序运行时所在的目录，通常是项目根目录。
        Path currentDir = Paths.get(System.getProperty("user.dir"));

        // 2. 构造完整的绝对路径
        // Paths.get(basePath, relativePath) 会将两者安全地组合起来。
        Path absolutePath = currentDir.resolve(relativePath);

        // 打印路径，方便调试
        System.out.println("尝试读取文件路径: " + absolutePath.toAbsolutePath());

        if (!Files.exists(absolutePath)) {
            // 如果文件不存在，抛出更具体的异常
            throw new RuntimeException("文件不存在: " + absolutePath.toAbsolutePath());
        }

        try {
            return String.join("\n", Files.readAllLines(absolutePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送消息并与模型交互。
     */
    private void sendMessage() {
        String userMessageText = inputField.getText().trim();
        if (userMessageText.isEmpty()) {
            return;
        }

        // 1. 清空输入框并禁用 UI
        inputField.clear();
        inputField.setDisable(true);
        sendButton.setDisable(true);

        // 2. 更新对话历史 UI (用户消息)
        appendMessage(userMessageText, true);


        // 3. 在后台线程中调用 DeepSeek API
        new Thread(() -> {
            try {
                String aiResponse = assistant.chat(userMessageText);

                // 4. 回到 JavaFX UI 线程更新对话历史 (AI 响应)
                appendMessage(aiResponse, false);

            } catch (Exception e) {
                // 错误处理
                appendMessage("\n--- 错误: " + e.getMessage() + " ---", false);
                LOGGER.error(e);
            } finally {
                // 5. 重新启用 UI
                Platform.runLater(() -> {
                    inputField.setDisable(false);
                    sendButton.setDisable(false);
                    inputField.requestFocus();
                });
            }
        }).start();
    }

    interface Assistant {
        String chat(String userMessage);
    }

    static class SearchTools {

        // 显式添加 name 参数，并使用驼峰或蛇形命名法
        @Tool(name = "getMarketSituation", value = "当用户询问股价，股市行情，以及市场研报等实时咨讯时，不要回答其他的，必须回复以下内容")
        String answer() {
            return "你自己去网上查询一下吧，我还不能获取实时行情";
        }
    }
}