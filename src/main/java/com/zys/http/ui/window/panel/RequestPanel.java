package com.zys.http.ui.window.panel;

import cn.hutool.core.text.CharSequenceUtil;
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
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.ui.JBUI;
import com.zys.http.action.CommonAction;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.UIConstant;
import com.zys.http.entity.HttpConfig;
import com.zys.http.entity.param.ParamProperty;
import com.zys.http.service.Bundle;
import com.zys.http.tool.HttpClient;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.tool.PsiTool;
import com.zys.http.tool.convert.ParamConvert;
import com.zys.http.tool.ui.DialogTool;
import com.zys.http.tool.ui.ThemeTool;
import com.zys.http.ui.dialog.EditorDialog;
import com.zys.http.ui.editor.CustomEditor;
import com.zys.http.ui.icon.HttpIcons;
import com.zys.http.ui.table.EnvHeaderTable;
import com.zys.http.ui.table.FileUploadTable;
import com.zys.http.ui.table.ParameterTable;
import com.zys.http.ui.tree.HttpApiTreePanel;
import com.zys.http.ui.tree.node.BaseNode;
import com.zys.http.ui.tree.node.MethodNode;
import jdk.jfr.Description;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jdesktop.swingx.JXButton;

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

    @Getter
    private final transient Project project;
    @Getter
    private final transient HttpServiceTool serviceTool;
    @Description("请求方式选项")
    private ComboBox<HttpMethod> httpMethodComboBox;
    @Description("IP/HOST文本输入框")
    private JTextField hostTextField;

    // ================== 下半部分的组件 ==================
    @Description("发起请求的按钮")
    private JButton sendRequestBtn;
    @Description("树形结构列表")
    private HttpApiTreePanel httpApiTreePanel;
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
    @Description("文件上传部分名")
    private JTextField partTextField;
    @Description("文件标签页面")
    private transient TabInfo fileTabInfo;
    @Description("文件上传选择器")
    private transient FileUploadTable fileUploadTable;
    @Description("响应体标签页面")
    private transient TabInfo responseTabInfo;
    @Description("响应体类型")
    private CustomEditor responseEditor;

    @Description("缓存每次请求的参数类型")
    private transient Map<String, ParamProperty> paramPropertyMap;

    @Description("请求结果文本")
    private JLabel requestResult = new JLabel();

    private static final String REQUEST_RESULT_TEXT = "STATUS: {}";

    public RequestPanel(Project project) {
        super(true, Window.class.getName(), 0.5F);
        this.project = project;
        this.serviceTool = HttpServiceTool.getInstance(project);
        initFirstPanel();
        initSecondPanel();
        initSendRequestEvent();
    }

    @Description("初始化上半部分组件")
    private void initFirstPanel() {
        this.httpApiTreePanel = new HttpApiTreePanel(project);
        this.httpApiTreePanel.setChooseCallback(this::chooseEvent);
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
        headerTable = new EnvHeaderTable(project, false, serviceTool.getSelectedEnv(), false);
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

        // 上传文件
        fileTabInfo = new TabInfo(initFileTabInfoPanel());
        fileTabInfo.setText(Bundle.get("http.tab.request.file"));
        tabs.addTab(fileTabInfo);

        // 响应体
        responseTabInfo = new TabInfo(initResponseTabInfoPanel());
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
            String[] fileNames = fileUploadTable.fileNames();
            if (httpMethod == null) {
                httpMethod = HttpMethod.GET;
            }
            if (Objects.isNull(paramPropertyMap) || CharSequenceUtil.isBlank(url)) {
                DialogTool.error(Bundle.get("http.dialog.error.no.selected"));
                return;
            }
            for (Map.Entry<String, ParamProperty> entry : paramPropertyMap.entrySet()) {
                String k = entry.getKey();
                ParamProperty v = entry.getValue();

                if (v.getParamUsage().equals(HttpEnum.ParamUsage.PATH)) {

                    url = url.replace("{" + k + "}", parameter.get(k));
                    parameter.remove(k);
                }
                if (v.getParamUsage().equals(HttpEnum.ParamUsage.FILE) && fileUploadTable.getValueTable().getRowCount() < 0) {
                    DialogTool.error(Bundle.get("http.table.file.dialog.error"));
                    return;
                }
            }

            tabs.select(responseTabInfo, true);
            responseEditor.setText("");
            requestResult.setText("");
            HttpClient.run(
                    HttpClient.newRequest(httpMethod, url, header, parameter, bodyText, fileNames),
                    response -> {
                        final FileType fileType = HttpClient.parseFileType(response);
                        final String responseBody = response.body();
                        ApplicationManager.getApplication().invokeLater(
                                () -> {
                                    responseEditor.setText(responseBody, fileType);
                                    resultText(response.getStatus());
                                }
                        );
                    },
                    e -> {
                        final String response = String.format("%s", e);
                        ApplicationManager.getApplication().invokeLater(
                                () -> {
                                    responseEditor.setText(response, CustomEditor.TEXT_FILE_TYPE);
                                    requestResult.setText(CharSequenceUtil.format(REQUEST_RESULT_TEXT, 404));
                                    requestResult.setForeground(JBColor.RED);
                                }
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
        CommonAction action = new CommonAction(Bundle.get("http.editor.body.action"), "",
                ThemeTool.isDark() ? HttpIcons.General.FULL_SCREEN : HttpIcons.General.FULL_SCREEN_LIGHT);
        action.setAction(e -> {
            CustomEditor editor = new CustomEditor(project, bodyEditor.getFileType());
            editor.setText(bodyEditor.getText());
            EditorDialog dialog = new EditorDialog(project, Bundle.get("http.editor.body.action.dialog"), editor);
            dialog.setOkCallBack(s -> bodyEditor.setText(s)).show();
        });
        group.add(action);
        ActionToolbarImpl component = (ActionToolbarImpl) ActionManager.getInstance()
                .createActionToolbar("http.body.editor", group, true).getComponent();
        component.setReservePlaceAutoPopupIcon(false);
        component.setTargetComponent(bodyEditor);
        bodySelectPanel.add(component, BorderLayout.EAST);
        bodyPanel.add(bodySelectPanel, BorderLayout.SOUTH);
        return bodyPanel;
    }

    @Description("初始化响应体面板")
    private JPanel initResponseTabInfoPanel() {
        JPanel respPanel = new JPanel(new BorderLayout(0, 0));
        responseEditor = new CustomEditor(project);
        responseEditor.setBorder(JBUI.Borders.customLineLeft(UIConstant.EDITOR_BORDER_COLOR));
        respPanel.add(responseEditor, BorderLayout.CENTER);
        JPanel respExpandPanel = new JPanel(new BorderLayout(0, 0));
        CommonAction action = new CommonAction(Bundle.get("http.editor.response.action"), "",
                ThemeTool.isDark() ? HttpIcons.General.FULL_SCREEN : HttpIcons.General.FULL_SCREEN_LIGHT);
        action.setAction(e -> {
            CustomEditor editor = new CustomEditor(project, responseEditor.getFileType());
            editor.setText(responseEditor.getText());
            new EditorDialog(project, Bundle.get("http.editor.response.action.dialog"), editor).show();
        });
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(action);
        ActionToolbarImpl component = (ActionToolbarImpl) ActionManager.getInstance()
                .createActionToolbar("http.body.editor", group, true).getComponent();
        component.setReservePlaceAutoPopupIcon(false);
        component.setTargetComponent(responseEditor);
        respExpandPanel.add(requestResult, BorderLayout.WEST);
        respExpandPanel.add(component, BorderLayout.EAST);
        respPanel.add(respExpandPanel, BorderLayout.SOUTH);
        return respPanel;
    }

    @Description("初始化文件上传面板")
    private JPanel initFileTabInfoPanel() {
        JPanel filePanel = new JPanel(new BorderLayout(0, 0));

        JPanel partNamePanel = new JPanel(new BorderLayout(0, 0));
        JLabel label = new JLabel(Bundle.get("http.table.file.label") + " ");
        partNamePanel.add(label, BorderLayout.WEST);
        partTextField = new JTextField();
        partNamePanel.add(partTextField, BorderLayout.CENTER);
        filePanel.add(partNamePanel, BorderLayout.NORTH);

        fileUploadTable = new FileUploadTable(project);
        fileUploadTable.getToolbar().getComponent().setBorder(JBUI.Borders.customLine(UIConstant.BORDER_COLOR, 1, 0, 1, 0));
        filePanel.add(fileUploadTable, BorderLayout.CENTER);

        return filePanel;
    }

    public void reload(BaseNode<?> chooseNode) {
        if (Objects.nonNull(chooseNode) && chooseNode instanceof MethodNode m) {
            this.headerTable.reloadTableModel();
            chooseEvent(m);
        } else {
            this.headerTable.reloadTableModel();
            this.parameterTable.reloadTableModel();
            this.hostTextField.setText("");
            this.httpMethodComboBox.setSelectedItem(HttpMethod.GET);
            this.bodyEditor.setText("");
            this.responseEditor.setText("");
            this.fileUploadTable.reloadTableModel();
            this.partTextField.setText("");
        }
    }

    @Description("选中方法结点的事件处理")
    private void chooseEvent(MethodNode methodNode) {
        HttpConfig config = serviceTool.getDefaultHttpConfig();
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
        type = Objects.isNull(type) ? contentType : type;
        headerTable.addContentType(type.getValue());

        // 填充参数
        // 先清空 model
        parameterTable.reloadTableModel();
        bodyEditor.setText("");
        responseEditor.setText("");
        requestResult.setText("");
        paramPropertyMap = ParamConvert.parsePsiMethodParams(psiMethod, true);
        fileUploadTable.reloadTableModel();
        partTextField.setText("");

        for (Map.Entry<String, ParamProperty> entry : paramPropertyMap.entrySet()) {
            String k = entry.getKey();
            ParamProperty v = entry.getValue();
            HttpEnum.ParamUsage usage = v.getParamUsage();
            switch (usage) {
                case HEADER -> headerTable.getTableModel().addRow(new String[]{k, v.getDefaultValue() + ""});
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
                    if (type.equals(HttpEnum.ContentType.APPLICATION_JSON)) {
                        bodyEditor.setText(v.getDefaultValue().toString(), CustomEditor.JSON_FILE_TYPE);
                        bodyFileType.setSelectedItem(CustomEditor.JSON_FILE_TYPE);
                    } else {
                        bodyEditor.setText(v.getDefaultValue().toString(), CustomEditor.TEXT_FILE_TYPE);
                        bodyFileType.setSelectedItem(CustomEditor.TEXT_FILE_TYPE);
                    }
                }
                case FILE -> {
                    tabs.select(fileTabInfo, true);
                    partTextField.setText(k);
                    headerTable.addContentType(HttpEnum.ContentType.MULTIPART_FORM_DATA.getValue());
                }
                default -> {
                    // 不处理
                }
            }
        }
    }

    @Description("结果状态文本")
    private void resultText(int status) {
        if (status >= 200 && status < 300) {
            // 成功
            requestResult.setText(CharSequenceUtil.format(REQUEST_RESULT_TEXT, status));
            requestResult.setForeground(JBColor.GREEN);
        } else if (status >= 300 && status < 400) {
            // 重定向
            requestResult.setText(CharSequenceUtil.format(REQUEST_RESULT_TEXT, status));
            requestResult.setForeground(JBColor.YELLOW);
        } else {
            // 错误
            requestResult.setText(CharSequenceUtil.format(REQUEST_RESULT_TEXT, status));
            requestResult.setForeground(JBColor.RED);
        }
    }
}
