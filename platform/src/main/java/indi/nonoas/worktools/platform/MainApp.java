package indi.nonoas.worktools.platform;

import indi.nonoas.worktools.platform.ui.component.FloatingTabPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        // 创建我们自定义的悬浮组件
        FloatingTabPane floatingPane = new FloatingTabPane();

        // 准备第一个页面的内容
        StackPane page1 = new StackPane(new Control() {
        });
        // 准备第二个页面的内容
        StackPane page2 = new StackPane(new Text("这是设置页"));

        // 使用 addTab 方法添加
        floatingPane.addTab("首页", page1);
        floatingPane.addTab("设置", page2);

        Scene scene = new Scene(floatingPane, 600, 400);

        stage.setTitle("悬浮 TabPane 测试");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}