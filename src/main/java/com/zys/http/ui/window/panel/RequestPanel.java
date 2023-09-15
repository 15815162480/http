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

    private void initFirstPanel() {
        JPanel firstPanel = new JPanel(new GridBagLayout());
        initHttpMethodOption();
        initHostOption();
        initHttpApiTreePanel();
        firstPanel.setBorder(JBUI.Borders.customLineTop(UIConstant.BORDER_COLOR));
        sendRequestBtn = new JXButton("发送");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        firstPanel.add(httpApiTreePanel, gbc);

        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 1;
        firstPanel.add(httpMethodComboBox, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = 1;
        firstPanel.add(hostTextField, gbc);
        gbc.weightx = 0;
        gbc.gridx = 2;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        firstPanel.add(sendRequestBtn, gbc);

        this.setFirstComponent(firstPanel);
    }

    @Description("初始化树形结构")
    private void initHttpApiTreePanel() {
        this.httpApiTreePanel = new HttpApiTreePanel(project);
        this.httpApiTreePanel.setChooseCallback(methodNode -> {
            HttpPropertyTool propertyTool = HttpPropertyTool.getInstance(project);
            HttpConfig config = propertyTool.getDefaultHttpConfig();
            String protocol = config.getProtocol().name().toLowerCase();
            String configHostValue = config.getHostValue();
            hostTextField.setText(protocol + "://" + configHostValue + methodNode.getFragment());
            httpMethodComboBox.setSelectedItem(methodNode.getValue().getHttpMethod());
        });
    }

    @Description("请求方式选项")
    private void initHttpMethodOption() {
        httpMethodComboBox = new ComboBox<>(HttpMethod.values());
        httpMethodComboBox.setSelectedItem(HttpMethod.GET);
        httpMethodComboBox.setFocusable(false);
    }

    @Description("host选项")
    private void initHostOption() {
        hostTextField = new JTextField();
        hostTextField.setColumns(10);
        hostTextField.setText(hostValue);
        hostTextField.addActionListener(e -> hostValue = hostTextField.getText());
    }

    private void initSecondPanel() {
        JPanel secondPanel = new JPanel(new BorderLayout(0, 0));
        secondPanel.setBorder(JBUI.Borders.customLineTop(UIConstant.BORDER_COLOR));

        tabs = new JBTabsImpl(project);
        // 初始化各个标签页面
        HttpPropertyTool tool = HttpPropertyTool.getInstance(project);
        headerTable = new EnvHeaderTable(project, false, tool.getSelectedEnv());
        ActionToolbar toolbar = headerTable.getToolbar();
        toolbar.getComponent().setBorder(JBUI.Borders.customLine(UIConstant.BORDER_COLOR, 0, 0, 1, 0));
        TabInfo tabInfo = new TabInfo(headerTable);
        tabInfo.setText("请求头");
        tabs.addTab(tabInfo);

        TabInfo tabInfo2 = new TabInfo(new JLabel("1111"));
        tabInfo2.setText("请求参数");
        tabs.addTab(tabInfo2);

        TabInfo tabInfo3 = new TabInfo(new JLabel("1111"));
        tabInfo3.setText("请求体");
        tabs.addTab(tabInfo3);


        TabInfo tabInfo4 = new TabInfo(new JLabel("1111"));
        tabInfo4.setText("返回值");
        tabs.addTab(tabInfo4);


        secondPanel.add(tabs.getComponent(), BorderLayout.NORTH);
        this.setSecondComponent(secondPanel);
    }
}
