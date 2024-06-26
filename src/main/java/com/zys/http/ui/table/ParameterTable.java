package com.zys.http.ui.table;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.lang.properties.PropertiesFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.zys.http.constant.HttpConstant;
import com.zys.http.extension.service.Bundle;
import com.zys.http.ui.dialog.EditorDialog;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.DefaultTableModel;
import java.util.LinkedHashMap;
import java.util.Map;

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
        String[] columnNames = {
                Bundle.get("http.api.tab.param.key"),
                Bundle.get("http.api.tab.param.value")
        };
        return new DefaultTableModel(null, columnNames);
    }

    @Override
    public void setModel(Map<String, String> headers) {
        String[] columnNames = {
                Bundle.get("http.api.tab.param.key"),
                Bundle.get("http.api.tab.param.value")
        };
        if (headers == null) {
            return;
        }
        String[][] rowData = new String[headers.size()][];
        int i = 0;
        for (Map.Entry<String, String> e : headers.entrySet()) {
            rowData[i] = new String[2];
            rowData[i][0] = e.getKey();
            rowData[i++][1] = e.getValue();
        }
        valueTable.setModel(new DefaultTableModel(rowData, columnNames));
    }

    @Override
    public void edit() {
        DefaultTableModel model = getTableModel();
        int count = model.getRowCount();
        StringBuilder all = new StringBuilder();
        for (int i = 0; i < count; i++) {
            String key = (String) model.getValueAt(i, 0);
            String value = model.getValueAt(i, 1) + "\n";
            all.append(CharSequenceUtil.format(HttpConstant.EDIT_AS_PROPERTIES_TEMPLATE, key, value));
        }
        EditorDialog dialog = new EditorDialog(project, Bundle.get("http.env.action.add.dialog.param.properties"),
                PropertiesFileType.INSTANCE, all.toString());
        dialog.setOkCallBack(text -> {
            if (CharSequenceUtil.isEmpty(text)) {
                ApplicationManager.getApplication().invokeLater(this::reloadTableModel);
                return;
            }
            String[] split = text.split("\n");
            Map<String, String> headerMap = new LinkedHashMap<>();
            for (String s : split) {
                if (!s.contains("=")) {
                    continue;
                }
                int i = s.indexOf("=");
                String key = s.substring(0, i).trim();
                String value = s.substring(i + 1).trim();
                if (CharSequenceUtil.isNotEmpty(key)) {
                    headerMap.put(key, value);
                }
            }
            ApplicationManager.getApplication().invokeLater(() -> {
                reloadTableModel();
                headerMap.forEach((k, v) -> getTableModel().addRow(new String[]{k, v}));
            });
        });
        dialog.show();
    }
}
