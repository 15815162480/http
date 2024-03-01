package com.zys.http.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.UIConstant;
import com.zys.http.entity.ReqHistory;
import com.zys.http.extension.service.Bundle;
import com.zys.http.tool.HistoryTool;
import com.zys.http.tool.HttpClient;
import com.zys.http.ui.tab.RequestTabs;
import com.zys.http.ui.table.EnvHeaderTable;
import com.zys.http.ui.table.ParameterTable;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * @author zhou ys
 * @since 2024-03-01
 */
public class HistoryDialog extends DialogWrapper {
    private final ReqHistory history;
    private final Project project;
    private final HistoryTool historyTool;

    public HistoryDialog(@NotNull Project project, Integer id) {
        super(project);
        this.project = project;
        this.historyTool = HistoryTool.getInstance(project);
        this.history = historyTool.getHistory(id);
        init();
        getRootPane().setMinimumSize(new Dimension(800, 600));
        getRootPane().setMaximumSize(new Dimension(800, 600));
        setOKButtonText(Bundle.get("http.text.ok"));
        setAutoAdjustable(true);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel secondPanel = new JPanel(new BorderLayout(0, 0));
        secondPanel.setBorder(JBUI.Borders.customLineTop(UIConstant.BORDER_COLOR));

        JPanel requestPanel = new JPanel(new BorderLayout(0, 0));
        // 请求方式下拉框
        HttpEnum.HttpMethod[] methods = Arrays.stream(HttpEnum.HttpMethod.values()).filter(o -> !o.equals(HttpEnum.HttpMethod.REQUEST))
                .toList().toArray(new HttpEnum.HttpMethod[]{});
        ComboBox<HttpEnum.HttpMethod> httpMethodComboBox = new ComboBox<>(methods);
        httpMethodComboBox.setSelectedItem(history.getMethod());
        requestPanel.add(httpMethodComboBox, BorderLayout.WEST);
        // 请求地址文本框
        JTextField hostTextField = new JTextField();
        requestPanel.add(hostTextField, BorderLayout.CENTER);
        hostTextField.setText(history.getHost() + history.getUri());
        // 发送按钮
        JXButton sendRequestBtn = new JXButton(Bundle.get("http.text.send"));
        requestPanel.add(sendRequestBtn, BorderLayout.EAST);
        secondPanel.add(requestPanel, BorderLayout.NORTH);

        // 标签页面
        JPanel tabsPanel = new JPanel(new BorderLayout(0, 0));
        RequestTabs requestTabs = new RequestTabs(project);
        tabsPanel.add(requestTabs, BorderLayout.CENTER);
        secondPanel.add(tabsPanel, BorderLayout.CENTER);

        EnvHeaderTable headerTable = requestTabs.getHeaderTable();
        headerTable.setModel(history.getHeaders());

        ParameterTable parameterTable = requestTabs.getParameterTable();
        parameterTable.setModel(history.getParams());

        requestTabs.getBodyEditor().setText(history.getBody(), HttpClient.parseFileType(history.getContentType()));

        requestTabs.getResponseEditor().setText(history.getRes());

        requestTabs.getFileUploadTable().setModel(history.getFileNames());

        return secondPanel;
    }
}
