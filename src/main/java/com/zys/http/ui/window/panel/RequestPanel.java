package com.zys.http.ui.window.panel;

import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.UIConstant;
import com.zys.http.entity.HttpConfig;
import com.zys.http.entity.param.ParamProperty;
import com.zys.http.service.Bundle;
import com.zys.http.tool.HttpClient;
import com.zys.http.tool.HttpPropertyTool;
import com.zys.http.tool.PsiTool;
import com.zys.http.tool.convert.ParamConvert;
import com.zys.http.ui.editor.CustomEditor;
import com.zys.http.ui.table.EnvHeaderTable;
import com.zys.http.ui.table.ParameterTable;
import com.zys.http.ui.tree.HttpApiTreePanel;
import jdk.jfr.Description;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

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

    @Description("请求头表格")
    private EnvHeaderTable headerTable;

    @Description("参数标签页面")
    private transient TabInfo parameterTabInfo;

    @Description("请求参数表格")
    private ParameterTable parameterTable;

    @Description("请求体标签页面")
    private transient TabInfo bodyTabInfo;

    @Description("请求体类型")
    private CustomEditor bodyEditor;

    @Description("响应体标签页面")
    private transient TabInfo responseTabInfo;

    @Description("响应体类型")
    private CustomEditor responseEditor;

    private final transient Project project;

    private transient Map<String, ParamProperty> paramPropertyMap;

    public RequestPanel(@NotNull Project project) {
        super(true, Window.class.getName(), 0.6F);
        this.project = project;
        initFirstPanel();
        initSecondPanel();
        initSendRequestEvent();
    }

    @Description("初始化上半部分组件")
    private void initFirstPanel() {
        JPanel firstPanel = new JPanel(new BorderLayout(0, 0));
        firstPanel.setBorder(JBUI.Borders.customLineTop(UIConstant.BORDER_COLOR));
        this.httpApiTreePanel = new HttpApiTreePanel(project);
        this.httpApiTreePanel.setChooseCallback(methodNode -> {
            HttpPropertyTool propertyTool = HttpPropertyTool.getInstance(project);
            HttpConfig config = propertyTool.getDefaultHttpConfig();
            String protocol = config.getProtocol().name().toLowerCase();
            String configHostValue = config.getHostValue();
            hostTextField.setText(protocol + "://" + configHostValue + methodNode.getFragment());
            HttpMethod httpMethod = methodNode.getValue().getHttpMethod();
            httpMethod = httpMethod.equals(HttpMethod.REQUEST) ? HttpMethod.GET : httpMethod;
            httpMethodComboBox.setSelectedItem(httpMethod);
            // 获取选中节点的参数类型
            PsiMethod psiMethod = (PsiMethod) methodNode.getValue().getPsiElement();
            HttpEnum.ContentType contentType = PsiTool.contentTypeHeader((PsiClass) psiMethod.getParent());
            HttpEnum.ContentType type = PsiTool.contentTypeHeader(psiMethod);
            type = httpMethod.equals(HttpMethod.GET) ? HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED : type;
            headerTable.addContentType(Objects.isNull(type) ? contentType.getValue() : type.getValue());

            // 填充参数
            // 先清空 model
            parameterTable.clearTableModel();
            bodyEditor.setText("");
            paramPropertyMap = ParamConvert.parsePsiMethodParams(psiMethod);
            for (Map.Entry<String, ParamProperty> entry : paramPropertyMap.entrySet()) {
                String k = entry.getKey();
                ParamProperty v = entry.getValue();
                HttpEnum.ParamUsage usage = v.getParamUsage();
                switch (usage) {
                    case PATH -> {
                        tabs.select(parameterTabInfo, true);
                        parameterTable.getTableModel().addRow(new Object[]{k, v.getDefaultValue()});
                    }
                    case URL -> {
                        if (httpMethod.equals(HttpMethod.POST)) {
                            // 将参数格式化成 username=a&password=a
                            String s = ParamConvert.buildParamPropertyUrlParameters(paramPropertyMap);
                            bodyEditor.setText(s, CustomEditor.TEXT_FILE_TYPE);
                            tabs.select(bodyTabInfo, true);
                        } else {
                            parameterTable.getTableModel().addRow(new Object[]{k, v.getDefaultValue()});
                            tabs.select(parameterTabInfo, true);
                        }
                    }
                    case BODY -> {
                        tabs.select(bodyTabInfo, true);
                        if (Objects.isNull(type)) {
                            if (contentType.equals(HttpEnum.ContentType.APPLICATION_JSON)) {
                                bodyEditor.setText(v.getDefaultValue().toString(), CustomEditor.JSON_FILE_TYPE);
                            } else {
                                bodyEditor.setText(v.getDefaultValue().toString(), CustomEditor.TEXT_FILE_TYPE);

                            }
                        } else {
                            bodyEditor.setText(v.getDefaultValue().toString(), CustomEditor.TEXT_FILE_TYPE);
                        }
                    }
                    default -> {
                        // 不处理
                    }
                }
            }

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
        JPanel tabsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.weightx = 1.0;
        gbc2.weighty = 1.0;
        gbc2.fill = GridBagConstraints.BOTH;
        tabs = new JBTabsImpl(project);
        // 请求头标签页
        HttpPropertyTool tool = HttpPropertyTool.getInstance(project);
        JPanel headerPanel = new JPanel(new BorderLayout(0, 0));
        headerTable = new EnvHeaderTable(project, false, tool.getSelectedEnv());
        ActionToolbar toolbar = headerTable.getToolbar();
        toolbar.getComponent().setBorder(JBUI.Borders.customLine(UIConstant.BORDER_COLOR, 0, 0, 1, 0));
        headerPanel.add(headerTable, BorderLayout.NORTH);
        TabInfo tabInfo = new TabInfo(headerPanel);
        tabInfo.setText(Bundle.get("http.tab.request.header"));
        tabs.addTab(tabInfo);

        // 请求参数
        parameterTable = new ParameterTable(project);
        JPanel paramPanel = new JPanel(new BorderLayout(0, 0));
        toolbar = parameterTable.getToolbar();
        toolbar.getComponent().setBorder(JBUI.Borders.customLine(UIConstant.BORDER_COLOR, 0, 0, 1, 0));
        paramPanel.add(parameterTable, BorderLayout.NORTH);
        parameterTabInfo = new TabInfo(paramPanel);
        parameterTabInfo.setText(Bundle.get("http.tab.request.param"));
        tabs.addTab(parameterTabInfo);


        // 请求体
        bodyEditor = new CustomEditor(project);
        bodyEditor.setName("BODY");
        bodyTabInfo = new TabInfo(bodyEditor);
        bodyTabInfo.setText(Bundle.get("http.tab.request.body"));
        tabs.addTab(bodyTabInfo);

        // 响应体
        responseEditor = new CustomEditor(project);
        responseEditor.setName("RESPONSE");
        responseTabInfo = new TabInfo(responseEditor);
        responseTabInfo.setText(Bundle.get("http.tab.request.return"));
        tabs.addTab(responseTabInfo);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = JBUI.insetsTop(3);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        tabsPanel.add(tabs.getComponent(), gbc2);
        secondPanel.add(tabsPanel, gbc);
        this.setSecondComponent(secondPanel);
    }


    private void initSendRequestEvent() {
        sendRequestBtn.addActionListener(event -> {
            String url = hostTextField.getText();
            Map<String, Object> header = headerTable.buildHttpHeader();
            Map<String, Object> parameter = parameterTable.buildHttpHeader();
            String bodyText = bodyEditor.getText();
            HttpMethod httpMethod = (HttpMethod) httpMethodComboBox.getSelectedItem();
            if (httpMethod == null) {
                httpMethod = HttpMethod.GET;
            }

            for (Map.Entry<String, ParamProperty> entry : paramPropertyMap.entrySet()) {
                String k = entry.getKey();
                ParamProperty v = entry.getValue();

                if (v.getParamUsage().equals(HttpEnum.ParamUsage.PATH)) {
                    url = url.replace("{" + k + "}", parameter.get(k).toString());
                    parameter.remove(k);
                }
            }

            tabs.select(responseTabInfo, true);
            HttpClient.run(
                    HttpClient.newRequest(httpMethod, url, header, parameter, bodyText),
                    response -> {
                        final FileType fileType = HttpClient.parseFileType(response);
                        final String responseBody = response.body();
                        ApplicationManager.getApplication().invokeLater(
                                () -> responseEditor.setText(responseBody, fileType)
                        );
                    },
                    e -> {
                        final String response = String.format("%s", e);
                        ApplicationManager.getApplication().invokeLater(
                                () -> responseEditor.setText(response, CustomEditor.TEXT_FILE_TYPE)
                        );
                    },
                    null
            );
        });
    }
}
