package com.zys.http.ui.table;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.lang.properties.PropertiesFileType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.zys.http.action.AddAction;
import com.zys.http.action.CustomAction;
import com.zys.http.action.EditAction;
import com.zys.http.action.RemoveAction;
import com.zys.http.entity.HttpConfig;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.topic.EditorDialogOkTopic;
import com.zys.http.ui.dialog.EditorDialog;
import jdk.jfr.Description;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.*;

import static com.zys.http.constant.HttpConstant.EDIT_AS_PROPERTIES_TEMPLATE;

/**
 * @author zys
 * @since 2023-09-03
 */
@Description("请求头表格")
public class EnvHeaderTable extends AbstractTable implements EditAsProperties {

    @Getter
    @Description("添加(true)/修改(false)")
    private final boolean isAdd;

    @Description("环境/请求头")
    private final boolean isEnv;

    @Description("选中的环境名, isAdd 为 true 时忽略")
    private String selectEnv;

    public EnvHeaderTable(Project project, boolean isAdd, String selectEnv, boolean isEnv) {
        super(project, true);
        this.isAdd = isAdd;
        this.isEnv = isEnv;
        if (!isAdd) {
            this.selectEnv = selectEnv;
        }
        init();
        initTopic();
    }

    protected void initTopic() {
        project.getMessageBus().connect().subscribe(EditorDialogOkTopic.TOPIC, new EditorDialogOkTopic() {
            @Override
            public void modify(String modifiedText, boolean isReplace) {
                // 没用到
            }

            @Override
            public void properties(String modifiedText, boolean isEnv, boolean isHeader) {
                if (!isHeader) {
                    return;
                }
                if (CharSequenceUtil.isEmpty(modifiedText)) {
                    valueTable.setModel(initTableModel());
                }
                String[] split = modifiedText.split("\n");
                Map<String, String> headerMap = new LinkedHashMap<>();

                Arrays.stream(split).filter(s -> s.contains("="))
                        .map(s -> {
                            int i = s.indexOf("=");
                            String key = s.substring(0, i).trim();
                            String value = s.substring(i + 1).trim();
                            return new String[]{key, value};
                        }).filter(s -> CharSequenceUtil.isNotBlank(s[0]))
                        .forEach(s -> headerMap.put(s[0], s[1]));

                // 如果是环境并且是编辑模式
                if (isEnv && !isAdd) {
                    HttpConfig httpConfig = serviceTool.getHttpConfig(selectEnv);
                    if (Objects.nonNull(httpConfig)) {
                        httpConfig.setHeaders(headerMap);
                        serviceTool.putHttpConfig(selectEnv, httpConfig);
                        ApplicationManager.getApplication().invokeLater(() -> reloadTableModel());
                    }
                }
                // 如果不是环境或添加模式
                if (!isEnv || isAdd) {
                    // 重新渲染请求头数据
                    String[] columnNames = {
                            Bundle.get("http.table.header"),
                            Bundle.get("http.table.value")
                    };

                    ApplicationManager.getApplication().invokeLater(() -> {
                        valueTable.setModel(new DefaultTableModel(null, columnNames));
                        headerMap.forEach((k, v) -> getTableModel().addRow(new String[]{k, v}));
                    });
                }
            }
        });
    }

    @Override
    protected @NotNull DefaultTableModel initTableModel() {
        String[] columnNames = {
                Bundle.get("http.table.header"),
                Bundle.get("http.table.value")
        };

        if (this.isAdd) {
            return new DefaultTableModel(null, columnNames);
        }
        HttpConfig httpConfig = serviceTool.getHttpConfig(selectEnv);
        if (Objects.isNull(httpConfig)) {
            return new DefaultTableModel(null, columnNames);
        }

        Map<String, String> headers = httpConfig.getHeaders();
        if (Objects.isNull(headers)) {
            return new DefaultTableModel(null, columnNames);
        }

        String[][] rowData = new String[headers.size()][];
        int i = 0;
        for (Map.Entry<String, String> e : headers.entrySet()) {
            rowData[i] = new String[2];
            rowData[i][0] = e.getKey();
            rowData[i++][1] = e.getValue();
        }

        return new DefaultTableModel(rowData, columnNames);
    }

    @Override
    protected @Nullable ActionToolbar initActionToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        AddAction addAction = new AddAction(Bundle.get("http.action.add"));
        addAction.setAction(event -> {
            DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
            int rowCount = model.getRowCount();
            if (rowCount == 0 || CharSequenceUtil.isNotEmpty((String) model.getValueAt(rowCount - 1, 0))) {
                model.addRow(new String[]{"", ""});
                valueTable.editCellAt(model.getRowCount() - 1, 0);
            }
        });
        group.add(addAction);

        RemoveAction removeAction = new RemoveAction(Bundle.get("http.action.remove"));
        removeAction.setAction(event -> {
            int selectedRow = valueTable.getSelectedRow();
            getTableModel().removeRow(selectedRow);
            int rowCount = valueTable.getRowCount();
            int newSelectRow = selectedRow == rowCount ? rowCount - 1 : selectedRow;
            valueTable.clearSelection();
            valueTable.getSelectionModel().setSelectionInterval(newSelectRow, newSelectRow);
        });
        removeAction.setEnabled(false);
        group.add(removeAction);
        EditAction action = new EditAction(Bundle.get("http.table.edit.properties"));
        action.setAction(e -> edit());
        group.add(action);
        return ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true);
    }

    @Override
    protected @NotNull TableModelListener initTableModelListener() {
        return e -> {
            DefaultTableModel model = (DefaultTableModel) e.getSource();
            int updateRow = e.getLastRow();
            int updateCol = e.getColumn();
            if (e.getType() == TableModelEvent.UPDATE) {// 最新一行且最新一行的请求头为空, 清除最新一行
                String header = (String) model.getValueAt(updateRow, updateCol);
                if (CharSequenceUtil.isEmpty(header)) {
                    model.removeRow(updateRow);
                }
            }
        };
    }

    @Override
    protected @NotNull ListSelectionListener initListSelectionListener() {
        return e -> getToolbar().getActions().forEach(v -> {
            if (v instanceof CustomAction c) {
                if (v instanceof AddAction || v instanceof EditAction) {
                    c.setEnabled(true);
                } else {
                    c.setEnabled(valueTable.getSelectedRow() != -1);
                }
            }
        });
    }

    public void addContentType(String contentType) {
        // 是否有 contentType
        TableModel model = valueTable.getModel();
        int rowCount = model.getRowCount();
        String header;
        boolean isChange = false;
        for (int i = 0; i < rowCount; i++) {
            header = (String) model.getValueAt(i, 0);
            if ("Content-Type".equals(header)) {
                model.setValueAt(contentType, i, 1);
                isChange = true;
            }
        }
        if (!isChange) {
            getTableModel().addRow(new String[]{"Content-Type", contentType});
        }
    }

    @Override
    public void reloadTableModel() {
        this.selectEnv = serviceTool.getSelectedEnv();
        valueTable.setModel(initTableModel());
    }

    public Map<String, String> buildHttpHeader() {
        Map<String, String> map = new HashMap<>();
        DefaultTableModel model = getTableModel();
        int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            map.put((String) model.getValueAt(i, 0), (String) model.getValueAt(i, 1));
        }
        return map;
    }

    @Override
    public void edit() {
        DefaultTableModel model = getTableModel();
        StringBuilder all = new StringBuilder();
        int count = model.getRowCount();
        for (int i = 0; i < count; i++) {
            String value = model.getValueAt(i, 1) + "\n";
            String key = (String) model.getValueAt(i, 0);
            all.append(CharSequenceUtil.format(EDIT_AS_PROPERTIES_TEMPLATE, key, value));
        }

        new EditorDialog(project, Bundle.get("http.editor.header.properties.dialog"),
                PropertiesFileType.INSTANCE, all.toString(), isEnv).show();
    }
}
