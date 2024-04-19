# WorkTool

> **NOTICE:**
> >功能通用化，涉及个人及集体隐私的文件切勿上传！


## 添加工具面板

### 1.源码形式添加

1. `indi.nonoas.worktools.view` 包下新建工具面板子包；
2. 一般新建子类继承 `javafx.scene.layout.Pane` 或其子类，用于布局组件。继承简单布局例如：VBox，HBox 类；
3. 在 MainStage 中添加按钮用于跳转到新建的工具面板；
4. `indi.nonoas.worktools.view.MainStage.initToolBar` 方法中添加按，以及初始化按钮点击事件。

### 2. 插件形式添加

1. 将主程序打包成 jar；
2. 新建 Java 项目用来开发插件，引入主程序 jar 包作为依赖；
3. 插件项目中提供一个实现了`indi.nonoas.worktools.ext.PluginService`接口的类，作为插件的主入口；
4. 项目类路径下添加 META-INF 目录，该目录下添加文件`plugin.json`，内容如下：
```json
{
  "name": "测试插件",
  "version": "1.0",
  "mainClass": "indi.testplugin.TestPlugin"
}
```
`name`：插件名称；<br>
`version`：插件版本；<br>
`mainClass`：插件主类名（实现了`indi.nonoas.worktools.ext.PluginService`接口的类）
5. 将插件打成 jar 包（不需要包含2中引入的主程序jar），置于主程序的`plugins`目录下，插件文件夹名称需与插件主类所在 jar 包文件名相同。

## 生成可执行 jar

终端指令 
```bash
mvn clean kotlin:compile package
```