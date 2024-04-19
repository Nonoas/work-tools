package indi.nonoas.worktools.view.workspace;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.h2.util.StringUtils;

import java.util.Optional;

/**
 * @author Nonoas
 * @date 2021/11/18
 */
public class JsonAnalysePane extends VBox {

    private final TextArea taCode = new TextArea();
    private final Button btnAnalyse = new Button("解析");

    /**
     * json解析按钮回调接口
     */
    private OnJsonAnalyzedListener onJsonAnalyzedListener;

    //声明私有静态对象，用volatile修饰
    private static volatile JsonAnalysePane instance;

    //私有构造器
    private JsonAnalysePane() {
        this(10);
    }

    private JsonAnalysePane(double space) {
        super(space);
        initView();
    }

    private void initView() {
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        getChildren().addAll(taCode, btnAnalyse);

        taCode.setPromptText("输入要解析的 Json");
        VBox.setVgrow(taCode, Priority.ALWAYS);

        btnAnalyse.setOnAction(
                event -> Optional.ofNullable(this.onJsonAnalyzedListener)
                        .ifPresent(listener -> listener.onJsonAnalyzed(event))
        );

        taCode.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!StringUtils.isNullOrEmpty(newValue)) {
                try {
                    JSONObject json = JSONObject.parseObject(newValue);
                    taCode.setText(JSON.toJSONString(json, SerializerFeature.PrettyFormat));
                } catch (JSONException ignored) {
                    // 如果格式错误，不转换就行了，这里什么的不需要写
                }
            }
        });
    }


    //对外提供获取实例对象的方法
    public static JsonAnalysePane getInstance() {
        return Optional.ofNullable(instance).orElseGet(() -> {
            //同步代码块
            synchronized (JsonAnalysePane.class) {
                instance = Optional.ofNullable(instance).orElseGet(JsonAnalysePane::new);
                return instance;
            }
        });
    }

    public String getJsonCode() {
        return taCode.getText();
    }

    public StringProperty jsonCodeProperty(){
        return taCode.textProperty();
    }

    public void setOnJsonAnalyzedListener(OnJsonAnalyzedListener onJsonAnalyzedListener) {
        this.onJsonAnalyzedListener = onJsonAnalyzedListener;
    }


    /**
     * 回调接口
     */
    @FunctionalInterface
    public interface OnJsonAnalyzedListener {
        void onJsonAnalyzed(Event event);
    }

}
