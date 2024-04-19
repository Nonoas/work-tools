package indi.nonoas.worktools.view.workspace;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import indi.nonoas.worktools.pojo.po.ModifyItemPo;
import indi.nonoas.worktools.pojo.vo.ModifyItemVo;
import indi.nonoas.worktools.ui.TaskHandler;
import indi.nonoas.worktools.ui.component.ExceptionAlter;
import indi.nonoas.worktools.ui.component.MyAlert;
import indi.nonoas.worktools.utils.DesktopUtil;
import indi.nonoas.worktools.utils.FileUtil;
import indi.nonoas.worktools.pojo.po.ModifyItemPo;
import indi.nonoas.worktools.pojo.vo.ModifyItemVo;
import indi.nonoas.worktools.ui.TaskHandler;
import indi.nonoas.worktools.ui.component.ExceptionAlter;
import indi.nonoas.worktools.ui.component.MyAlert;
import indi.nonoas.worktools.utils.DesktopUtil;
import indi.nonoas.worktools.utils.FileUtil;
import indi.nonoas.worktools.view.MainStage;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.Objects;

/**
 * @author Nonoas
 * @date 2022/3/18
 */
public class ModifyListView extends ListView<ModifyItemVo> {

    private OnItemDelete onItemDelete;

    public ModifyListView() {
        setPlaceholder(new Label("没有数据"));
        setCellFactory(param -> new ModifyListCell());
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setOnItemDelete(OnItemDelete onItemDelete) {
        this.onItemDelete = onItemDelete;
    }

    public interface OnItemDelete {
        void apply(ListCell<ModifyItemVo> cell);
    }

    /**
     * 列表单元格
     */
    protected static class ModifyListCell extends ListCell<ModifyItemVo> {

        public ModifyListCell() {

        }

        /**
         * 双击事件处理
         */
        private final EventHandler<MouseEvent> doubleClickHandler = new WeakReference<EventHandler<MouseEvent>>(
                event -> {
                    // 鼠标左键双击事件
                    if (2 == event.getClickCount() && event.getButton() == MouseButton.PRIMARY) {
                        event.consume();
                        DesktopUtil.open(
                                new File(Objects.requireNonNull(ModifyListCell.this.getItem().getAbsolutePath()))
                        );
                    }
                }
        ).get();

        @Override
        protected void updateItem(ModifyItemVo item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty || null != item) {
                setText(item.toString());
                initContextMenu();
                removeEventHandler(MouseEvent.MOUSE_CLICKED, doubleClickHandler);
                addEventHandler(MouseEvent.MOUSE_CLICKED, doubleClickHandler);
            } else {
                // 移除监听和右键菜单
                removeEventHandler(MouseEvent.MOUSE_CLICKED, doubleClickHandler);
                ContextMenu contextMenu = getContextMenu();
                if (contextMenu != null) {
                    contextMenu.getItems().clear();
                }
                // 设置图像和文字为 null
                setText(null);
                setGraphic(null);
            }
        }

        /**
         * 初始化右键菜单
         */
        private void initContextMenu() {

            MenuItem miOpenDescFile = new MenuItem("修改说明");
            miOpenDescFile.setOnAction(event ->
                    DesktopUtil.open(new File(getItem().getAbsolutePath() + File.separator + "0-修改说明.md"))
            );

            MenuItem mCopyNum = new MenuItem("复制单号");
            mCopyNum.setOnAction(event -> {
                Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable tText = new StringSelection(getItem().getModifyNum());
                clip.setContents(tText, null);
            });


            MenuItem miRename = new MenuItem("重命名");
            miRename.setOnAction(event -> {
                ModifyItemVo item = getItem();
                String path = item.getAbsolutePath();
                if (StrUtil.isBlank(path)) {
                    return;
                }
                TextInputDialog dialog = new TextInputDialog(item.getDesc()) {{
                    setHeaderText(null);
                    initOwner(MainStage.Companion.getInstance());
                }};
                dialog.showAndWait().ifPresent(s -> renameItem(item, path, s));
            });

            MenuItem miDel = new MenuItem("删除");
            miDel.setOnAction(event -> new TaskHandler<Integer>()
                    .whenCall(() -> {
                        try {
                            String absolutePath = getItem().getAbsolutePath();
                            FileUtil.deleteFile(new File(Objects.requireNonNull(absolutePath)));
                            return Db.use().del("modify_items", "modify_num", getItem().getModifyNum());
                        } catch (SQLException e) {
                            ExceptionAlter.error(e);
                        }
                        return null;
                    }).andThen(integer -> {
                        ModifyListView lv = (ModifyListView) getListView();
                        if (lv.onItemDelete != null) {
                            lv.onItemDelete.apply(ModifyListCell.this);
                        }
                    }).handle());
            ContextMenu menu = new ContextMenu(miOpenDescFile, mCopyNum, miRename, miDel);
            setContextMenu(menu);
        }

        private void renameItem(ModifyItemVo item, String path, String desc) {
            String newName = String.format("%s-%s-%s", item.getModifyNum(), desc, item.getVersionNO());
            String newPath = path.substring(0, path.lastIndexOf(File.separator) + 1) + newName;
            boolean b = new File(path).renameTo(new File(newPath));
            if (!b) {
                new MyAlert(Alert.AlertType.ERROR, "重命名失败").showAndWait();
            } else {
                item.setModifyReason(desc);
                item.setDesc(desc);
                item.setAbsolutePath(newPath);
                updateToDB(item);
                // 更新当前 item 内容
                updateItem(item, false);
            }
        }

        private void updateToDB(ModifyItemVo item) {
            ModifyItemPo po = item.convertPo();
            po.setModifyTime(System.currentTimeMillis());
            Entity entity = Entity.create("modify_items")
                    .set("absolute_path", po.getAbsolutePath())
                    .set("modify_time", System.currentTimeMillis())
                    .set("desc", po.getDesc());
            Entity where = Entity.create(entity.getTableName()).set("modify_num", po.getModifyNum());
            try {
                Db.use().update(entity, where);
            } catch (SQLException e) {
                ExceptionAlter.error(e);
            }
        }
    }

}
