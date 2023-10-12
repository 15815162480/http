package com.zys.http.ui.table;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.lang.properties.PropertiesFileType;
import com.intellij.openapi.project.Project;
import com.zys.http.service.Bundle;
import com.zys.http.ui.dialog.EditorDialog;
import com.zys.http.ui.editor.CustomEditor;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.DefaultTableModel;
import java.util.Objects;

/**
 * @author zys
 * @since 2023-09-16
 */
@Description("请求参数(path、param 参数)表格")
public class ParameterTable extends EnvHeaderTable {

    private static final String TEMPLATE = "{}={}";

    public ParameterTable(Project project) {
        super(project, true);
    }

    @Override
    protected @NotNull DefaultTableModel initTableModel() {
        String[] columnNames = {
                Bundle.get("http.table.param.key"),
                Bundle.get("http.table.param.value")
        };
        return new DefaultTableModel(null, columnNames);
    }

    @Override
    public void edit() {
        CustomEditor editor = new CustomEditor(project, PropertiesFileType.INSTANCE);
        DefaultTableModel model = getTableModel();
        int count = model.getRowCount();
        StringBuilder all = new StringBuilder();
        for (int i = 0; i < count; i++) {
            String key = (String) model.getValueAt(i, 0);
            String value = model.getValueAt(i, 1) + "\n";
            all.append(CharSequenceUtil.format(TEMPLATE, key, value));
        }
        editor.setText(all.toString());
        reloadTableModel();
        EditorDialog dialog = new EditorDialog(project, Bundle.get("http.editor.body.action.dialog"), editor);
        dialog.setOkCallBack(s -> {
            if (Objects.isNull(s) || s.isEmpty()) {
                return;
            }
            String[] split = s.split("\n");
            for (String param : split) {
                if (param.contains("=")) {
                    int idx = param.indexOf("=");
                    String key = param.substring(0, idx).trim();
                    String value = param.substring(idx + 1).trim();
                    if (CharSequenceUtil.isNotBlank(key)) {
                        getTableModel().addRow(new String[]{key, value});
                    }
                }
            }
        });
        dialog.show();
    }
}
