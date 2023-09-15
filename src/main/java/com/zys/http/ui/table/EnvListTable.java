package com.zys.http.ui.table;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.zys.http.action.AddAction;
import com.zys.http.action.CustomAction;
import com.zys.http.action.EditAction;
import com.zys.http.action.RemoveAction;
import com.zys.http.entity.HttpConfig;
import com.zys.http.service.Bundle;
import com.zys.http.ui.dialog.EnvAddOrEditDialog;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.util.Map;
import java.util.Set;

/**
 * @author zys
 * @since 2023-09-03
 */
@Description("环境列表表格")
public class EnvListTable extends AbstractTable {

    public EnvListTable(@NotNull Project project) {
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
        Map<String, HttpConfig> httpConfigs = httpPropertyTool.getHttpConfigs();
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
        AddAction addAction = new AddAction(Bundle.get("http.action.add"), "Add");
        addAction.setAction(event -> new EnvAddOrEditDialog(project, true, "", this).show());
        group.add(addAction);

        RemoveAction removeAction = new RemoveAction(Bundle.get("http.action.remove"), "Remove");
        removeAction.setAction(event -> {
            DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
            int selectedRow = valueTable.getSelectedRow();
            httpPropertyTool.removeHttpConfig((String) model.getValueAt(selectedRow, 0));
            model.removeRow(selectedRow);
        });
        removeAction.setEnabled(false);
        group.add(removeAction);

        EditAction editAction = new EditAction(Bundle.get("http.action.edit"), "Edit");
        editAction.setAction(event -> {
            DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
            String envName = (String) model.getValueAt(valueTable.getSelectedRow(), 0);
            new EnvAddOrEditDialog(project, false, envName, this).show();
        });
        editAction.setEnabled(false);
        group.add(editAction);
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
}