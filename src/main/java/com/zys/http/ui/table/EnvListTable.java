package com.zys.http.ui.table;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.zys.http.action.*;
import com.zys.http.constant.HttpEnum;
import com.zys.http.entity.HttpConfig;
import com.zys.http.extension.topic.EnvChangeTopic;
import com.zys.http.extension.topic.EnvListChangeTopic;
import com.zys.http.extension.service.Bundle;
import com.zys.http.tool.ui.DialogTool;
import com.zys.http.ui.dialog.EnvAddOrEditDialog;
import jdk.jfr.Description;
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
    public EnvListTable(Project project) {
        super(project, false, false);
        init();
        initTopic();
    }

    private void initTopic() {
        project.getMessageBus().connect().subscribe(EnvListChangeTopic.TOPIC, new EnvListChangeTopic() {
            @Override
            public void save(String name, HttpConfig config) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    getTableModel().addRow(new String[]{name, config.getProtocol().toString(), config.getHostValue()});
                    serviceTool.putHttpConfig(name, config);
                    reloadTableModel();
                });
            }

            @Override
            public void edit(String name, HttpConfig config) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
                    int selectedRow = valueTable.getSelectedRow();
                    model.setValueAt(config.getProtocol().toString(), selectedRow, 1);
                    model.setValueAt(config.getHostValue(), selectedRow, 2);
                    serviceTool.putHttpConfig(name, config);
                    reloadTableModel();
                    project.getMessageBus().syncPublisher(EnvChangeTopic.TOPIC).change();
                });
            }

            @Override
            public void remove(String name) {
                serviceTool.removeHttpConfig(name);
                ApplicationManager.getApplication().invokeLater(() -> reloadTableModel());
            }
        });
    }

    @Override
    public @NotNull DefaultTableModel initTableModel() {
        String[] columnNames = {
                Bundle.get("http.env.table.env.name"),
                Bundle.get("http.env.table.protocol"),
                Bundle.get("http.env.table.ip")
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

        AddAction addAction = new AddAction(Bundle.get("http.common.action.add"));
        addAction.setAction(event -> new EnvAddOrEditDialog(project, true, "").show());
        group.add(addAction);

        RemoveAction removeAction = new RemoveAction(Bundle.get("http.common.action.remove"));
        removeAction.setAction(event -> {
            DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
            int selectedRow = valueTable.getSelectedRow();
            String envName = (String) model.getValueAt(selectedRow, 0);
            if (envName.equals(serviceTool.getSelectedEnv())) {
                DialogTool.error(Bundle.get("http.env.action.remove.msg"));
                return;
            }
            serviceTool.removeHttpConfig(envName);
            model.removeRow(selectedRow);
        });
        removeAction.setEnabled(false);
        group.add(removeAction);

        EditAction editAction = new EditAction(Bundle.get("http.common.action.edit"));
        editAction.setAction(event -> {
            DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
            String envName = (String) model.getValueAt(valueTable.getSelectedRow(), 0);
            new EnvAddOrEditDialog(project, false, envName).show();
        });

        editAction.setEnabled(false);
        group.add(editAction);

        HttpConfigExportAction exportAction = new HttpConfigExportAction(Bundle.get("http.env.icon.postman.action.export.selected.env"), HttpEnum.ExportEnum.SPECIFY_ENV);
        exportAction.setAction(e -> {
            DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
            String envName = (String) model.getValueAt(valueTable.getSelectedRow(), 0);
            exportAction.initAction(envName);
        });
        exportAction.setEnabled(false);
        group.add(exportAction);

        return ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true);
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
