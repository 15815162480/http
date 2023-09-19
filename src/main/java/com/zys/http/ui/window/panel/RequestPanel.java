package com.zys.http.ui.window.panel;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.impl.FileTypeRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.ui.JBUI;
import com.zys.http.action.ShowAction;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.UIConstant;
import com.zys.http.entity.HttpConfig;
import com.zys.http.entity.param.ParamProperty;
import com.zys.http.service.Bundle;
import com.zys.http.tool.HttpClient;
import com.zys.http.tool.HttpPropertyTool;
import com.zys.http.tool.PsiTool;
import com.zys.http.tool.convert.ParamConvert;
import com.zys.http.ui.dialog.EditorDialog;
import com.zys.http.ui.editor.CustomEditor;
import com.zys.http.ui.icon.HttpIcons;
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

    @Description("发起请求的按钮")
    private JButton sendRequestBtn;

    @Description("树形结构列表")
    private HttpApiTreePanel httpApiTreePanel;

    // ================== 下半部分的组件 ==================

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

    @Description("请求体编辑区")
    private CustomEditor bodyEditor;

    @Description("请求体类型")
    private ComboBox<FileType> bodyFileType;

    @Description("响应体标签页面")
    private transient TabInfo responseTabInfo;

    @Description("响应体类型")
    private CustomEditor responseEditor;

    private final transient Project project;

    private transient Map<String, ParamProperty> paramPropertyMap;

    public RequestPanel(@NotNull Project project) {
        super(true, Window.class.getName(), 0.5F);
        this.project = project;
        initFirstPanel();
        initSecondPanel();
        initSendRequestEvent();
    }

    @Description("初始化上半部分组件")
    private void initFirstPanel() {
        this.httpApiTreePanel = new HttpApiTreePanel(project);
        this.httpApiTreePanel.setChooseCallback(methodNode -> {
            HttpConfig config = HttpPropertyTool.getInstance(project).getDefaultHttpConfig();
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
            responseEditor.setText("");
            paramPropertyMap = ParamConvert.parsePsiMethodParams(psiMethod);

            for (Map.Entry<String, ParamProperty> entry : paramPropertyMap.entrySet()) {
                String k = entry.getKey();
                ParamProperty v = entry.getValue();
                HttpEnum.ParamUsage usage = v.getParamUsage();
                switch (usage) {
                    case PATH -> {
                        tabs.select(parameterTabInfo, true);
                        parameterTable.getTableModel().addRow(new String[]{k, v.getDefaultValue() + ""});
                    }
                    case URL -> {
                        if (httpMethod.equals(HttpMethod.POST)) {
                            // 将参数格式化成 username=a&password=a
                            String s = ParamConvert.buildParamPropertyUrlParameters(paramPropertyMap);
                            bodyEditor.setText(s, CustomEditor.TEXT_FILE_TYPE);
                            tabs.select(bodyTabInfo, true);
                            bodyFileType.setSelectedItem(CustomEditor.TEXT_FILE_TYPE);
                        } else {
                            parameterTable.getTableModel().addRow(new String[]{k, v.getDefaultValue() + ""});
                            tabs.select(parameterTabInfo, true);
                        }
                    }
                    case BODY -> {
                        tabs.select(bodyTabInfo, true);
                        if (Objects.isNull(type)) {
                            if (contentType.equals(HttpEnum.ContentType.APPLICATION_JSON)) {
                                bodyEditor.setText(v.getDefaultValue().toString(), CustomEditor.JSON_FILE_TYPE);
                                bodyFileType.setSelectedItem(CustomEditor.JSON_FILE_TYPE);
                            } else {
                                bodyEditor.setText(v.getDefaultValue().toString(), CustomEditor.TEXT_FILE_TYPE);
                                bodyFileType.setSelectedItem(CustomEditor.TEXT_FILE_TYPE);
                            }
                        } else {
                            bodyEditor.setText(v.getDefaultValue().toString(), CustomEditor.TEXT_FILE_TYPE);
                            bodyFileType.setSelectedItem(CustomEditor.TEXT_FILE_TYPE);
                        }
                    }
                    default -> {
                        // 不处理
                    }
                }
            }

        });
        this.setFirstComponent(httpApiTreePanel);
    }

    @Description("初始化下半部分组件")
    private void initSecondPanel() {
        JPanel secondPanel = new JPanel(new BorderLayout(0, 0));
        secondPanel.setBorder(JBUI.Borders.customLineTop(UIConstant.BORDER_COLOR));

        JPanel requestPanel = new JPanel(new BorderLayout(0, 0));
        // 请求方式下拉框
        HttpMethod[] methods = Arrays.stream(HttpMethod.values()).filter(o -> !o.equals(HttpMethod.REQUEST))
                .toList().toArray(new HttpMethod[]{});
        httpMethodComboBox = new ComboBox<>(methods);
        requestPanel.add(httpMethodComboBox, BorderLayout.WEST);
        // 请求地址文本框
        hostTextField = new JTextField();
        requestPanel.add(hostTextField, BorderLayout.CENTER);
        // 发送按钮
        sendRequestBtn = new JXButton(Bundle.get("http.text.send"));
        requestPanel.add(sendRequestBtn, BorderLayout.EAST);
        secondPanel.add(requestPanel, BorderLayout.NORTH);

        // 标签页面
        JPanel tabsPanel = new JPanel(new BorderLayout(0, 0));
        tabs = new JBTabsImpl(project);
        // 请求头标签页
        HttpPropertyTool tool = HttpPropertyTool.getInstance(project);
        headerTable = new EnvHeaderTable(project, false, tool.getSelectedEnv());
        headerTable.getToolbar().getComponent().setBorder(JBUI.Borders.customLineBottom(UIConstant.BORDER_COLOR));
        TabInfo tabInfo = new TabInfo(headerTable);
        tabInfo.setText(Bundle.get("http.tab.request.header"));
        tabs.addTab(tabInfo);
        // 请求参数
        parameterTable = new ParameterTable(project);
        parameterTable.getToolbar().getComponent().setBorder(JBUI.Borders.customLineBottom(UIConstant.BORDER_COLOR));
        parameterTabInfo = new TabInfo(parameterTable);
        parameterTabInfo.setText(Bundle.get("http.tab.request.param"));
        tabs.addTab(parameterTabInfo);
        // 请求体
        bodyTabInfo = new TabInfo(initBodyTabInfoPanel());
        bodyTabInfo.setText(Bundle.get("http.tab.request.body"));
        tabs.addTab(bodyTabInfo);
        // 响应体
        responseEditor = new CustomEditor(project);
        responseEditor.setBorder(JBUI.Borders.customLineLeft(UIConstant.EDITOR_BORDER_COLOR));
        responseTabInfo = new TabInfo(responseEditor);
        responseTabInfo.setText(Bundle.get("http.tab.request.return"));
        tabs.addTab(responseTabInfo);

        tabs.getComponent().setBorder(JBUI.Borders.customLineLeft(UIConstant.EDITOR_BORDER_COLOR));
        tabsPanel.add(tabs.getComponent(), BorderLayout.CENTER);
        secondPanel.add(tabsPanel, BorderLayout.CENTER);
        this.setSecondComponent(secondPanel);
    }

    @Description("初始化发送请求按钮")
    private void initSendRequestEvent() {
        sendRequestBtn.addActionListener(event -> {
            String url = hostTextField.getText();
            Map<String, String> header = headerTable.buildHttpHeader();
            Map<String, String> parameter = parameterTable.buildHttpHeader();
            String bodyText = bodyEditor.getText();
            HttpMethod httpMethod = (HttpMethod) httpMethodComboBox.getSelectedItem();
            if (httpMethod == null) {
                httpMethod = HttpMethod.GET;
            }

            for (Map.Entry<String, ParamProperty> entry : paramPropertyMap.entrySet()) {
                String k = entry.getKey();
                ParamProperty v = entry.getValue();

                if (v.getParamUsage().equals(HttpEnum.ParamUsage.PATH)) {
                    url = url.replace("{" + k + "}", parameter.get(k));
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

    @Description("初始化请求体面板")
    private JPanel initBodyTabInfoPanel() {
        JPanel bodyPanel = new JPanel(new BorderLayout(0, 0));
        bodyEditor = new CustomEditor(project);
        bodyEditor.setName("BODY");
        bodyEditor.setBorder(JBUI.Borders.customLineLeft(UIConstant.EDITOR_BORDER_COLOR));
        bodyPanel.add(bodyEditor, BorderLayout.CENTER);
        JLabel label = new JLabel(Bundle.get("http.editor.body.label"));
        bodyFileType = new ComboBox<>(new FileType[]{
                CustomEditor.TEXT_FILE_TYPE,
                CustomEditor.JSON_FILE_TYPE,
                CustomEditor.XML_FILE_TYPE
        });
        bodyFileType.setFocusable(false);
        bodyFileType.setRenderer(new FileTypeRenderer());
        bodyFileType.addItemListener(e -> {
            ItemSelectable item = e.getItemSelectable();
            if (Objects.isNull(item)) {
                return;
            }
            Object[] selects = item.getSelectedObjects();
            if (Objects.isNull(selects) || selects.length < 1) {
                return;
            }
            Object select = selects[0];
            if (select instanceof FileType fileType) {
                bodyEditor.setFileType(fileType);
            }
        });

        JPanel bodySelectPanel = new JPanel(new BorderLayout(0, 0));
        bodySelectPanel.add(label, BorderLayout.WEST);
        bodySelectPanel.add(bodyFileType, BorderLayout.CENTER);
        DefaultActionGroup group = new DefaultActionGroup();
        ShowAction action = new ShowAction(Bundle.get("http.editor.body.action"), "", HttpIcons.General.FULL_SCREEN);
        action.setAction(e -> {
            CustomEditor editor = new CustomEditor(project, bodyEditor.getFileType());
            editor.setText(bodyEditor.getText());
            EditorDialog dialog = new EditorDialog(project, Bundle.get("http.editor.body.action.dialog"), editor);
            dialog.setOkCallBack(s -> bodyEditor.setText(s)).show();
        });
        group.add(action);
        ActionToolbarImpl component = (ActionToolbarImpl) ActionManager.getInstance().createActionToolbar("http.body.editor", group, true).getComponent();
        component.setReservePlaceAutoPopupIcon(false);
        bodySelectPanel.add(component, BorderLayout.EAST);
        bodyPanel.add(bodySelectPanel, BorderLayout.SOUTH);
        return bodyPanel;
    }

    public void reload() {
        this.headerTable.reloadTableModel();
        this.parameterTable.clearTableModel();
        this.hostTextField.setText("");
        this.httpMethodComboBox.setSelectedItem(HttpMethod.GET);
    }
}
