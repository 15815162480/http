package com.zys.http.ui.table;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.zys.http.action.*;
import com.zys.http.entity.HttpConfig;
import com.zys.http.service.Bundle;
import com.zys.http.service.NotifyService;
import com.zys.http.tool.velocity.VelocityTool;
import com.zys.http.ui.dialog.EnvAddOrEditDialog;
import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author zys
 * @since 2023-09-03
 */
@Description("环境列表表格")
public class EnvListTable extends AbstractTable {

    @Setter
    @Description("修改选中环境回调")
    private transient Consumer<Void> editOKCb;

    public EnvListTable(Project project) {
        super(project, false);
        init();
    }

    @Override
    public @NotNull DefaultTableModel initTableModel() {
        String[] columnNames = {
                Bundle.get("http.table.env.config.name"),
                Bundle.get("http.table.env.config.protocol"),
                Bundle.get("http.table.env.config.ip")
        };
        // 获取存储的所有配置, 再构建
        Map<String, HttpConfig> httpConfigs = serviceTool.getHttpConfigs();
        Set<Map.Entry<String, HttpConfig>> entries = httpConfigs.entrySet();
        int i = 0;
        String[][] rowData = new String[entries.size()][];
        for (Map.Entry<String, HttpConfig> e : entries) {
            rowData[i] = new String[columnNames.length];
            rowData[i][0] = e.getKey();
            HttpConfig value = e.getValue();
            rowData[i][1] = value.getProtocol().toString();
            rowData[i++][2] = value.getHostValue();
        }
        return new DefaultTableModel(rowData, columnNames);
    }

    @Override
    protected @Nullable ActionToolbar initActionToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        AddAction addAction = new AddAction(Bundle.get("http.action.add"));
        addAction.setAction(event -> {
            EnvAddOrEditDialog dialog = new EnvAddOrEditDialog(project, true, "");
            dialog.setAddCallback((name, config) -> getTableModel().addRow(new String[]{name, config.getProtocol().toString(), config.getHostValue()}));
            dialog.show();
        });
        group.add(addAction);

        RemoveAction removeAction = new RemoveAction(Bundle.get("http.action.remove"));
        removeAction.setAction(event -> {
            DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
            int selectedRow = valueTable.getSelectedRow();
            serviceTool.removeHttpConfig((String) model.getValueAt(selectedRow, 0));
            model.removeRow(selectedRow);
        });
        removeAction.setEnabled(false);
        group.add(removeAction);

        EditAction editAction = new EditAction(Bundle.get("http.action.edit"));
        editAction.setAction(event -> {
            DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
            String envName = (String) model.getValueAt(valueTable.getSelectedRow(), 0);
            EnvAddOrEditDialog dialog = new EnvAddOrEditDialog(project, false, envName);
            dialog.setEditCallback((name, config) -> {
                int selectedRow = valueTable.getSelectedRow();
                model.setValueAt(config.getProtocol().toString(), selectedRow, 1);
                model.setValueAt(config.getHostValue(), selectedRow, 2);
                editOKCb.accept(null);
            });
            dialog.show();
        });

        editAction.setEnabled(false);
        group.add(editAction);

        CommonAction exportOne = exportOneAction();
        group.add(exportOne);

        return ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true);
    }

    @NotNull
    @Description("导出单个环境配置操作")
    private CommonAction exportOneAction() {
        CommonAction exportOne = new CommonAction(Bundle.get("http.action.export.select.env"), "Export Current Env", HttpIcons.General.EXPORT);
        exportOne.setAction(event -> {
            DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
            String envName = (String) model.getValueAt(valueTable.getSelectedRow(), 0);
            HttpConfig config = serviceTool.getHttpConfig(envName);
            if (null == config) {
                NotifyService.instance(project).error(Bundle.get("http.message.export.current.env.error"));
                return;
            }
            VirtualFile selectedFile = createFileChooser(project);
            if (null == selectedFile) {
                NotifyService.instance(project).error("http.message.export.unselect.folder");
                return;
            }

            try {
                VelocityTool.exportEnv(envName, config, selectedFile.getPath());
                NotifyService.instance(project).info(Bundle.get("http.message.export.success"));
            } catch (IOException ex) {
                NotifyService.instance(project).error(Bundle.get("http.message.export.fail"));
            }
        });
        exportOne.setEnabled(false);
        return exportOne;
    }

    @Override
    protected @NotNull TableModelListener initTableModelListener() {
        return e -> {
            if (e.getType() != TableModelEvent.DELETE || ((DefaultTableModel) e.getSource()).getRowCount() > 0) {
                return;
            }
            getToolbar().getActions().forEach(v -> {
                if (v instanceof CustomAction c && !(v instanceof AddAction)) {
                    c.setEnabled(false);
                }
            });
        };
    }

    @Description("创建文件选择对话框")
    private VirtualFile createFileChooser(Project project) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        descriptor.setTitle(Bundle.get("http.dialog.env.export"));
        FileChooserFactory.getInstance().createFileChooser(descriptor, project, this);
        return FileChooser.chooseFile(descriptor, project, null);
    }
}
