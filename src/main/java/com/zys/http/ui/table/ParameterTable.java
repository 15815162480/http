package com.zys.http.ui.table;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.project.Project;
import com.zys.http.action.AddAction;
import com.zys.http.action.CustomAction;
import com.zys.http.service.Bundle;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

/**
 * @author zys
 * @since 2023-09-16
 */
@Description("请求参数(path、param 参数)表格")
public class ParameterTable extends EnvHeaderTable {
    public ParameterTable(Project project) {
        super(project, true, "");
    }

    @Override
    protected @NotNull DefaultTableModel initTableModel() {
        Object[] columnNames = {
                Bundle.get("http.table.param.key"),
                Bundle.get("http.table.param.value")
        };
        return new DefaultTableModel(null, columnNames);
    }

    @Override
    protected @NotNull TableModelListener initTableModelListener() {
        return e -> {
            DefaultTableModel model = (DefaultTableModel) e.getSource();
            int updateRow = e.getLastRow();
            int updateCol = e.getColumn();
            switch (e.getType()) {
                case TableModelEvent.INSERT -> valueTable.editCellAt(model.getRowCount() - 1, 0);
                case TableModelEvent.UPDATE -> {
                    // 最新一行且最新一行的参数为空, 清除最新一行
                    String header = (String) model.getValueAt(updateRow, updateCol);
                    if (CharSequenceUtil.isEmpty(header)) {
                        model.removeRow(updateRow);
                    }
                }
                case TableModelEvent.DELETE -> {
                    if (model.getRowCount() <= 0) {
                        getToolbar().getActions().forEach(v -> {
                            if (v instanceof CustomAction c && !(v instanceof AddAction)) {
                                c.setEnabled(false);
                            }
                        });
                    }
                }
                default -> valueTable.editCellAt(-1, -1);
            }
        };
    }
}
