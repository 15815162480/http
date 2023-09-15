package com.zys.http.ui.window.panel;

import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.UIConstant;
import com.zys.http.entity.HttpConfig;
import com.zys.http.service.Bundle;
import com.zys.http.tool.HttpPropertyTool;
import com.zys.http.ui.table.EnvHeaderTable;
import com.zys.http.ui.tree.HttpApiTreePanel;
import jdk.jfr.Description;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

import static com.zys.http.constant.HttpEnum.HttpMethod;

/**
 * @author zys
 * @since 2023-08-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RequestPanel extends JBSplitter {

    // ================== 上半部分的组件 ==================
    @Description("请求方式选项")
    private ComboBox<HttpMethod> httpMethodComboBox;

    @Description("IP/HOST文本输入框")
    private JTextField hostTextField;

    private JButton sendRequestBtn;

    @Description("设置的IP/HOST")
    private String hostValue = "";

    @Description("树形结构列表")
    private HttpApiTreePanel httpApiTreePanel;

    // ================== 上半部分的组件 ==================

    @Description("标签栏")
    private transient JBTabs tabs;

    private EnvHeaderTable headerTable;

    private final transient Project project;

    public RequestPanel(@NotNull Project project) {
        super(true, Window.class.getName(), 0.6F);
        this.project = project;
        initFirstPanel();
        initSecondPanel();
    }

    @Description("初始化上半部分组件")
    private void initFirstPanel() {
        JPanel firstPanel = new JPanel(new BorderLayout(0,0));
        firstPanel.setBorder(JBUI.Borders.customLineTop(UIConstant.BORDER_COLOR));
        this.httpApiTreePanel = new HttpApiTreePanel(project);
        this.httpApiTreePanel.setChooseCallback(methodNode -> {
            HttpPropertyTool propertyTool = HttpPropertyTool.getInstance(project);
            HttpConfig config = propertyTool.getDefaultHttpConfig();
            String protocol = config.getProtocol().name().toLowerCase();
            String configHostValue = config.getHostValue();
            hostTextField.setText(protocol + "://" + configHostValue + methodNode.getFragment());
            HttpMethod httpMethod = methodNode.getValue().getHttpMethod();
            httpMethodComboBox.setSelectedItem(httpMethod.equals(HttpMethod.REQUEST) ? HttpMethod.GET : httpMethod);
        });
        firstPanel.add(httpApiTreePanel, BorderLayout.NORTH);

        this.setFirstComponent(firstPanel);
    }

    @Description("初始化下半部分组件")
    private void initSecondPanel() {
        JPanel secondPanel = new JPanel(new GridBagLayout());
        secondPanel.setBorder(JBUI.Borders.customLineTop(UIConstant.BORDER_COLOR));

        GridBagConstraints gbc = new GridBagConstraints();
        // 请求方式下拉框
        HttpMethod[] methods = Arrays.stream(HttpMethod.values()).filter(o -> !o.equals(HttpMethod.REQUEST))
                .toList().toArray(new HttpMethod[]{});
        httpMethodComboBox = new ComboBox<>(methods);
        httpMethodComboBox.setSelectedItem(HttpMethod.GET);
        httpMethodComboBox.setFocusable(false);
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        secondPanel.add(httpMethodComboBox, gbc);

        // 请求地址文本框
        hostTextField = new JTextField();
        hostTextField.setColumns(10);
        hostTextField.setText(hostValue);
        hostTextField.addActionListener(e -> hostValue = hostTextField.getText());
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = 1;
        secondPanel.add(hostTextField, gbc);

        // 发送按钮
        sendRequestBtn = new JXButton(Bundle.get("http.text.send"));
        gbc.weightx = 0;
        gbc.gridx = 2;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        secondPanel.add(sendRequestBtn, gbc);

        // 标签页面
        JPanel tabsPanel = new JPanel(new BorderLayout(0, 0));
        tabs = new JBTabsImpl(project);
        // 请求头标签页
        HttpPropertyTool tool = HttpPropertyTool.getInstance(project);
        headerTable = new EnvHeaderTable(project, false, tool.getSelectedEnv());
        ActionToolbar toolbar = headerTable.getToolbar();
        toolbar.getComponent().setBorder(JBUI.Borders.customLine(UIConstant.BORDER_COLOR, 0, 0, 1, 0));
        TabInfo tabInfo = new TabInfo(headerTable);
        tabInfo.setText(Bundle.get("http.tab.request.header"));
        tabs.addTab(tabInfo);

        TabInfo tabInfo2 = new TabInfo(new JLabel("1111"));
        tabInfo2.setText(Bundle.get("http.tab.request.param"));
        tabs.addTab(tabInfo2);

        TabInfo tabInfo3 = new TabInfo(new JLabel("1111"));
        tabInfo3.setText(Bundle.get("http.tab.request.body"));
        tabs.addTab(tabInfo3);

        TabInfo tabInfo4 = new TabInfo(new JLabel("1111"));
        tabInfo4.setText(Bundle.get("http.tab.request.return"));
        tabs.addTab(tabInfo4);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = JBUI.insetsTop(3);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        tabsPanel.add(tabs.getComponent(), BorderLayout.NORTH);
        secondPanel.add(tabsPanel, gbc);
        this.setSecondComponent(secondPanel);
    }
}
