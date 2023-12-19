package com.zys.http.ui.table;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.lang.properties.PropertiesFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.zys.http.constant.HttpConstant;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.topic.EditorDialogOkTopic;
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
        super(project, true, "", false);
        initTopic();
    }

    @Override
    protected void initTopic() {
        project.getMessageBus().connect().subscribe(EditorDialogOkTopic.TOPIC, new EditorDialogOkTopic() {

            @Override
            public void modify(String modifiedText, boolean isReplace) {
                // 没用到
            }

            @Override
            public void properties(String modifiedText, boolean isEnv, boolean isHeader) {
                if (isHeader || isEnv) {
                    return;
                }
                if (CharSequenceUtil.isEmpty(modifiedText)) {
                    valueTable.setModel(initTableModel());
                }
                String[] split = modifiedText.split("\n");
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
            }
        });

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
        DefaultTableModel model = getTableModel();
        int count = model.getRowCount();
        StringBuilder all = new StringBuilder();
        for (int i = 0; i < count; i++) {
            String key = (String) model.getValueAt(i, 0);
            String value = model.getValueAt(i, 1) + "\n";
            all.append(CharSequenceUtil.format(HttpConstant.EDIT_AS_PROPERTIES_TEMPLATE, key, value));
        }
        new EditorDialog(project, Bundle.get("http.editor.param.properties.dialog"),
                PropertiesFileType.INSTANCE, all.toString(), false).show();
    }
}
