package com.zys.http.ui.table;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.zys.http.action.*;
import com.zys.http.constant.HttpEnum;
import com.zys.http.entity.HttpConfig;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.topic.EnvChangeTopic;
import com.zys.http.tool.ui.DialogTool;
import com.zys.http.ui.dialog.EnvAddOrEditDialog;
import jdk.jfr.Description;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author zys
 * @since 2023-09-03
 */
@Description("环境列表表格")
public class EnvListTable extends AbstractTable {

    @Setter
    @Description("修改选中环境回调")
    private transient Runnable editOKCb;

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
            String envName = (String) model.getValueAt(selectedRow, 0);
            if (envName.equals(serviceTool.getSelectedEnv())) {
                DialogTool.error(Bundle.get("http.table.env.remove.msg"));
                return;
            }
            serviceTool.removeHttpConfig(envName);
            model.removeRow(selectedRow);
        });
        removeAction.setEnabled(false);
        group.add(removeAction);

        EditAction editAction = createEditAction();
        group.add(editAction);

        HttpConfigExportAction exportAction = new HttpConfigExportAction(Bundle.get("http.action.export.select.env"), HttpEnum.ExportEnum.SPECIFY_ENV);
        exportAction.setAction(e -> {
            DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
            String envName = (String) model.getValueAt(valueTable.getSelectedRow(), 0);
            exportAction.initAction(envName);
        });
        exportAction.setEnabled(false);
        group.add(exportAction);

        return ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true);
    }

    @NotNull
    private EditAction createEditAction() {
        EditAction editAction = new EditAction(Bundle.get("http.action.edit"));
        editAction.setAction(event -> {
            DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
            String envName = (String) model.getValueAt(valueTable.getSelectedRow(), 0);
            EnvAddOrEditDialog dialog = new EnvAddOrEditDialog(project, false, envName);
            dialog.setEditCallback((name, config) -> {
                int selectedRow = valueTable.getSelectedRow();
                model.setValueAt(config.getProtocol().toString(), selectedRow, 1);
                model.setValueAt(config.getHostValue(), selectedRow, 2);
                editOKCb.run();
            });
            dialog.show();
            project.getMessageBus().syncPublisher(EnvChangeTopic.TOPIC).change();
        });

        editAction.setEnabled(false);
        return editAction;
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

    @Override
    protected @NotNull ListSelectionListener initListSelectionListener() {
        return e -> {
            ActionToolbar toolbar = getToolbar();
            if (!e.getValueIsAdjusting() && Objects.nonNull(toolbar)) {
                toolbar.getActions().forEach(v -> {
                    if (v instanceof CustomAction c && !(v instanceof AddAction)) {
                        c.setEnabled(valueTable.getSelectedRow() != -1);
                    }
                });
            }
        };
    }
}
