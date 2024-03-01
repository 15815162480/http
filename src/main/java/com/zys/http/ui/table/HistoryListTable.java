package com.zys.http.ui.table;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.zys.http.action.AddAction;
import com.zys.http.action.CommonAction;
import com.zys.http.action.CustomAction;
import com.zys.http.action.RemoveAction;
import com.zys.http.constant.HttpConstant;
import com.zys.http.entity.ReqHistory;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.topic.HisListChangeTopic;
import com.zys.http.tool.HistoryTool;
import com.zys.http.tool.HttpClient;
import com.zys.http.ui.dialog.HistoryDialog;
import com.zys.http.ui.window.RequestTabWindow;
import com.zys.http.ui.window.panel.RequestPanel;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhou ys
 * @since 2024-03-01
 */
@Description("历史列表表格")
public class HistoryListTable extends AbstractTable {
    private final transient HistoryTool historyTool;

    public HistoryListTable(Project project) {
        super(project, false);
        historyTool = HistoryTool.getInstance(project);
        init();
        initTopic();
    }

    private void initTopic() {
        project.getMessageBus().connect().subscribe(HisListChangeTopic.TOPIC, new HisListChangeTopic() {
            @Override
            public void save(ReqHistory config) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    historyTool.saveHistory(config);
                    reloadTableModel();
                });
            }

            @Override
            public void remove(Integer id) {
                historyTool.deleteHistory(id);
                ApplicationManager.getApplication().invokeLater(() -> reloadTableModel());
            }
        });
    }

    @Override
    protected @NotNull DefaultTableModel initTableModel() {
        String[] columnNames = {
                Bundle.get("http.history.table.header.no"),
                Bundle.get("http.history.table.header.method"),
                Bundle.get("http.history.table.header.url"),
                Bundle.get("http.history.table.header.time")
        };
        Map<Integer, ReqHistory> histories = historyTool.getHistories();
        int i = 0;
        List<ReqHistory> list = histories.values().stream().sorted(Collections.reverseOrder((o1, o2) -> {
            LocalDateTime parse = LocalDateTime.parse(o1.getTime().replace(" ", "T"));
            LocalDateTime parse1 = LocalDateTime.parse(o2.getTime().replace(" ", "T"));
            return parse.compareTo(parse1);
        })).toList();
        String[][] rowData = new String[histories.size()][];
        for (ReqHistory history : list) {
            rowData[i] = new String[columnNames.length];
            rowData[i][0] = String.valueOf(history.getId());
            rowData[i][1] = history.getMethod().toString();
            rowData[i][2] = history.getHost() + history.getUri();
            rowData[i++][3] = history.getTime();
        }

        return new DefaultTableModel(rowData, columnNames);
    }

    @Override
    protected @Nullable ActionToolbar initActionToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        RemoveAction removeAction = new RemoveAction(Bundle.get("http.action.remove"));
        removeAction.setAction(e -> {
            DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
            int selectedRow = valueTable.getSelectedRow();
            int id = Integer.parseInt((String) model.getValueAt(selectedRow, 0));
            historyTool.deleteHistory(id);
            model.removeRow(selectedRow);
        });
        removeAction.setEnabled(false);
        group.add(removeAction);

        CommonAction readAction = new CommonAction(Bundle.get("http.history.action.look"), "", AllIcons.Actions.Show);
        readAction.setAction(e -> new HistoryDialog(project, Integer.parseInt((String) valueTable.getModel().getValueAt(valueTable.getSelectedRow(), 0))).show());
        readAction.setEnabled(false);
        group.add(readAction);

        // 将请求生成到请求面板
        CommonAction renderAction = new CommonAction(Bundle.get("http.history.action.generate"), "", AllIcons.Actions.MoveToButton);
        renderAction.setAction(e -> {
            ToolWindowManager manager = ToolWindowManager.getInstance(Objects.requireNonNull(e.getProject()));
            ToolWindow toolWindow = manager.getToolWindow(HttpConstant.PLUGIN_NAME);
            if (toolWindow == null) {
                return;
            }
            ContentManager contentManager = toolWindow.getContentManager();
            Content content = contentManager.getContent(0);
            if (Objects.isNull(content) || !(content.getComponent() instanceof RequestTabWindow requestTabWindow)) {
                return;
            }
            DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
            int selectedRow = valueTable.getSelectedRow();
            int id = Integer.parseInt((String) model.getValueAt(selectedRow, 0));
            ReqHistory history = historyTool.getHistory(id);
            if (history == null) {
                return;
            }

            ApplicationManager.getApplication().invokeLater(() -> {
                RequestPanel requestPanel = requestTabWindow.getRequestPanel();
                requestPanel.getHostTextField().setText(history.getHost() + history.getUri());
                requestPanel.getHttpMethodComboBox().setSelectedItem(history.getMethod());
                requestPanel.getRequestTabs().getFileUploadTable().setModel(history.getFileNames());
                requestPanel.getRequestTabs().getParameterTable().setModel(history.getParams());
                requestPanel.getRequestTabs().getHeaderTable().setModel(history.getHeaders());
                requestPanel.getRequestTabs().getBodyEditor().setText(history.getBody(), HttpClient.parseFileType(history.getContentType()));
                requestPanel.getRequestTabs().getBodyFileType().setSelectedItem(HttpClient.parseFileType(history.getContentType()));
                contentManager.setSelectedContent(content);
            });
        });
        renderAction.setEnabled(false);
        group.add(renderAction);

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
