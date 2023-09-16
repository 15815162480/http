package com.zys.http.ui.table;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.zys.http.action.AddAction;
import com.zys.http.action.CustomAction;
import com.zys.http.action.RemoveAction;
import com.zys.http.entity.HttpConfig;
import com.zys.http.service.Bundle;
import jdk.jfr.Description;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

/**
 * @author zys
 * @since 2023-09-03
 */
@Description("请求头表格")
public class EnvHeaderTable extends AbstractTable {

    @Getter
    @Description("添加(true)/修改(false)")
    private final boolean isAdd;

    @Description("选中的环境名, isAdd 为 true 时忽略")
    private String selectEnv;

    public EnvHeaderTable(@NotNull Project project, boolean isAdd, String selectEnv) {
        super(project, true);
        this.isAdd = isAdd;
        if (!isAdd) {
            this.selectEnv = selectEnv;
        }
        init();
    }

    @Override
    protected @NotNull DefaultTableModel initTableModel() {
        // 构建列信息
        Vector<String> columnNames = new Vector<>();
        columnNames.add(Bundle.get("http.table.header"));
        columnNames.add(Bundle.get("http.table.value"));
        Vector<Vector<String>> rowData = new Vector<>();

        if (!this.isAdd) {
            HttpConfig httpConfig = httpPropertyTool.getHttpConfig(selectEnv);
            if (Objects.nonNull(httpConfig)) {
                Map<String, Object> headers = httpConfig.getHeaders();
                if (Objects.nonNull(headers)) {
                    headers.forEach((k, v) -> {
                        Vector<String> vector = new Vector<>(2);
                        vector.add(k);
                        vector.add((String) v);
                        rowData.add(vector);
                    });
                }
            }
        }

        return new DefaultTableModel(rowData, columnNames);
    }

    @Override
    protected @Nullable ActionToolbar initActionToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        AddAction addAction = new AddAction(Bundle.get("http.action.add"), "Add");
        addAction.setAction(event -> {
            DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
            int rowCount = model.getRowCount();
            if (rowCount == 0 || CharSequenceUtil.isNotEmpty((String) model.getValueAt(rowCount - 1, 0))) {
                model.addRow(new String[]{"", ""});
                valueTable.editCellAt(model.getRowCount() - 1, 0);
            }
        });
        group.add(addAction);

        RemoveAction removeAction = new RemoveAction(Bundle.get("http.action.remove"), "Remove");
        removeAction.setAction(event -> {
            DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
            valueTable.getSelectionModel().setSelectionInterval(0, 0);
            model.removeRow(valueTable.getSelectedRow());
        });
        removeAction.setEnabled(false);
        group.add(removeAction);

        return ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true);
    }

    @Override
    protected @NotNull TableModelListener initTableModelListener() {
        return e -> {
            DefaultTableModel model = (DefaultTableModel) e.getSource();
            int updateRow = e.getLastRow();
            int updateCol = e.getColumn();
            switch (e.getType()) {
                case TableModelEvent.UPDATE -> {
                    // 最新一行且最新一行的请求头为空, 清除最新一行
                    String header = (String) model.getValueAt(updateRow, updateCol);
                    if (CharSequenceUtil.isEmpty(header) || isDuplicateData(valueTable, header)) {
                        model.removeRow(updateRow);
                    }
                }
                case TableModelEvent.DELETE -> {
                    if (model.getRowCount() > 0) {
                        return;
                    }
                    getToolbar().getActions().forEach(v -> {
                        if (v instanceof CustomAction c && !(v instanceof AddAction)) {
                            c.setEnabled(false);
                        }
                    });
                }
                default -> valueTable.editCellAt(-1, -1);
            }
        };
    }

    @Description("请求头是否重复")
    private static boolean isDuplicateData(JTable table, String header) {
        for (int row = 0; row < table.getRowCount() - 1; row++) {
            String existingHeader = (String) table.getValueAt(row, 0);
            if (header.equals(existingHeader)) {
                return true;
            }
        }
        return false;
    }

    public void addContentType(String contentType) {
        // 是否有 contentType
        TableModel model = valueTable.getModel();
        int rowCount = model.getRowCount();
        String header;
        boolean isChange = false;
        for (int i = 0; i < rowCount; i++) {
            header = (String) model.getValueAt(i, 0);
            if (header.equals("Content-Type")) {
                model.setValueAt(contentType, i, 1);
                isChange = true;
            }
        }
        if (!isChange) {
            getTableModel().addRow(new String[]{"Content-Type", contentType});
        }
    }


    @SuppressWarnings("rawtypes")
    public Map<String, Object> buildHttpHeader() {
        Map<String, Object> map = new HashMap<>();
        DefaultTableModel model = getTableModel();
        Vector<Vector> vector = model.getDataVector();
        for (Vector<?> v1 : vector) {
            map.put((String) v1.get(0), v1.get(1) + "");
        }
        return map;
    }
}
