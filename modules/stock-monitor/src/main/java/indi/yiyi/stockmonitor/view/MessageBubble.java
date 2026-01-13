package indi.yiyi.stockmonitor.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import one.jpro.platform.mdfx.MarkdownView;

/**
 * 消息气泡组件：用于显示左对齐（AI）或右对齐（用户）的消息。
 */
class MessageBubble extends HBox {

    public MessageBubble(String message, boolean isUser) {
        super();
        this.setPadding(new Insets(5, 10, 5, 10)); // HBox 内边距
        Insets padding = new Insets(8, 12, 8, 12);

        if (isUser) {
            Label messageLabel = new Label(message);
            messageLabel.setPadding(padding); // Label 内边距
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(400); // 限制气泡最大宽度
            // 用户消息：右对齐，蓝色背景
            this.setAlignment(Pos.CENTER_RIGHT);
            messageLabel.setStyle("-fx-background-color: #0078FF; -fx-text-fill: white; -fx-background-radius: 15;");
            this.getChildren().add(messageLabel);
        } else {
            // AI 消息：左对齐，灰色背景
            this.setAlignment(Pos.CENTER_LEFT);
            MarkdownView markdownView = new MarkdownView(message);
            markdownView.setPadding(padding);
            markdownView.setStyle("""
                    -fx-background-color: #E0E0E0;
                    -fx-text-fill: black;
                    -fx-background-radius: 15;
                    -fx-font-size: 8px !important;
                    """);
            this.getChildren().add(markdownView);
        }

    }
}