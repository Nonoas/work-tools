package indi.nonoas.worktools.platform.service;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import indi.nonoas.worktools.platform.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LlmSearchService {

    private static final Logger LOGGER = LogManager.getLogger(LlmSearchService.class);
    private static final String DEFAULT_BASE_URL = "https://api.moonshot.cn/v1";
    private static final String DEFAULT_MODEL = "kimi-k2-turbo-preview";
    private static final String DEFAULT_SYSTEM_MESSAGE = "你是 WorkTools 内置助手，请基于用户输入提供简洁、准确、可执行的中文回答。";

    private static volatile Assistant assistant;

    private LlmSearchService() {
    }

    public static String chat(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "";
        }
        return getAssistant().chat(prompt.trim());
    }

    private static Assistant getAssistant() {
        if (assistant != null) {
            return assistant;
        }
        synchronized (LlmSearchService.class) {
            if (assistant == null) {
                assistant = createAssistant();
            }
            return assistant;
        }
    }

    private static Assistant createAssistant() {
        String apiKey = resolveApiKey();
        String systemMessage = resolveSystemMessage();

        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl(DEFAULT_BASE_URL)
                .apiKey(apiKey)
                .modelName(DEFAULT_MODEL)
                .build();

        return AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .systemMessageProvider(memoryId -> systemMessage)
                .build();
    }

    private static String resolveApiKey() {
        String apiKey = System.getenv("KIMI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("MOONSHOT_API_KEY");
        }
        if (apiKey == null || apiKey.isBlank()) {
            try {
                apiKey = FileUtil.readTextFromProjectRelativePath("config/apikey");
            } catch (Exception ex) {
                throw new IllegalStateException("未找到 LLM API Key，请设置 KIMI_API_KEY 或提供 config/apikey", ex);
            }
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("LLM API Key 为空，请检查 KIMI_API_KEY 或 config/apikey");
        }
        return apiKey.trim();
    }

    private static String resolveSystemMessage() {
        try {
            String content = FileUtil.readTextFromProjectRelativePath("config/context");
            if (content == null || content.isBlank()) {
                return DEFAULT_SYSTEM_MESSAGE;
            }
            return content;
        } catch (Exception ex) {
            LOGGER.warn("Load LLM context failed, fallback to default prompt", ex);
            return DEFAULT_SYSTEM_MESSAGE;
        }
    }

    interface Assistant {
        String chat(String userMessage);
    }
}
