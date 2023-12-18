package com.zys.http.ui.tab;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import com.zys.http.action.CommonAction;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.UIConstant;
import com.zys.http.entity.param.ParamProperty;
import com.zys.http.service.Bundle;
import com.zys.http.extension.topic.EditorDialogOkTopic;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.tool.convert.ParamConvert;
import com.zys.http.tool.ui.ComboBoxTool;
import com.zys.http.tool.ui.ThemeTool;
import com.zys.http.ui.dialog.EditorDialog;
import com.zys.http.ui.editor.CustomEditor;
import com.zys.http.ui.icon.HttpIcons;
import com.zys.http.ui.table.EnvHeaderTable;
import com.zys.http.ui.table.FileUploadTable;
import com.zys.http.ui.table.ParameterTable;
import jdk.jfr.Description;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhou ys
 * @since 2023-10-20
 */
@Getter
@Description("API 页面的标签选项栏")
public class RequestTabs extends JBTabsImpl {

    @Description("项目对象")
    private final transient Project project;
    @Getter
    private final transient HttpServiceTool serviceTool;

    @Description("参数标签页面")
    private transient TabInfo headerTabInfo;
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

    @Description("请求结果文本")
    private final JLabel requestResult = new JLabel();

    private static final String REQUEST_RESULT_TEXT = "STATUS: {}";

    public RequestTabs(@NotNull Project project) {
        super(project);
        this.project = project;
        this.serviceTool = HttpServiceTool.getInstance(project);
        init();
    }

    private void init() {
        requestHeaderTab();
        requestParamTab();
        requestBodyTab();
        requestFileTab();
        responseTab();
        this.getComponent().setBorder(JBUI.Borders.customLineLeft(UIConstant.EDITOR_BORDER_COLOR));
        initTopic();
    }

    private void initTopic() {
        MessageBus messageBus = project.getMessageBus();
        MessageBusConnection connect = messageBus.connect();
        connect.subscribe(EditorDialogOkTopic.TOPIC, new EditorDialogOkTopic() {
            @Override
            public void modify(String modifiedText, boolean isReplace) {
                if (isReplace) {
                    bodyEditor.setText(modifiedText);
                }
            }

            @Override
            public void properties(String modifiedText, boolean isHeader) {
                // 没用到
            }
        });
    }

    @Description("请求头标签页")
    private void requestHeaderTab() {
        this.headerTable = new EnvHeaderTable(this.project, false, this.serviceTool.getSelectedEnv(), false);
        this.headerTable.getToolbar().getComponent().setBorder(JBUI.Borders.customLineBottom(UIConstant.BORDER_COLOR));
        this.headerTabInfo = new TabInfo(this.headerTable);
        this.headerTabInfo.setText(Bundle.get("http.tab.request.header"));
        this.addTab(this.headerTabInfo);
    }

    @Description("请求参数标签页")
    private void requestParamTab() {
        this.parameterTable = new ParameterTable(this.project);
        this.parameterTable.getToolbar().getComponent().setBorder(JBUI.Borders.customLineBottom(UIConstant.BORDER_COLOR));
        this.parameterTabInfo = new TabInfo(this.parameterTable);
        this.parameterTabInfo.setText(Bundle.get("http.tab.request.param"));
        this.addTab(this.parameterTabInfo);
    }

    @Description("请求体标签页")
    private void requestBodyTab() {
        JPanel bodyPanel = new JPanel(new BorderLayout(0, 0));
        this.bodyEditor = new CustomEditor(project);
        this.bodyEditor.setName("BODY");
        bodyPanel.setBorder(JBUI.Borders.customLineLeft(UIConstant.EDITOR_BORDER_COLOR));
        bodyPanel.add(this.bodyEditor, BorderLayout.CENTER);
        JLabel label = new JLabel(Bundle.get("http.editor.body.label"));
        this.bodyFileType = ComboBoxTool.fileTypeComboBox(CustomEditor.FILE_TYPE_LIST, e -> {
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
        action.setAction(e -> new EditorDialog(project, Bundle.get("http.editor.body.action.dialog"), bodyEditor.getFileType(), bodyEditor.getText()).show());
        group.add(action);
        ActionToolbarImpl component = (ActionToolbarImpl) ActionManager.getInstance()
                .createActionToolbar("http.body.editor", group, true).getComponent();
        component.setReservePlaceAutoPopupIcon(false);
        component.setTargetComponent(bodyEditor);
        bodySelectPanel.add(component, BorderLayout.EAST);
        bodyPanel.add(bodySelectPanel, BorderLayout.SOUTH);

        this.bodyTabInfo = new TabInfo(bodyPanel);
        this.bodyTabInfo.setText(Bundle.get("http.tab.request.body"));
        this.addTab(bodyTabInfo);
    }

    @Description("请求文件标签页")
    private void requestFileTab() {
        JPanel filePanel = new JPanel(new BorderLayout(0, 0));

        JPanel partNamePanel = new JPanel(new BorderLayout(0, 0));
        JLabel label = new JLabel(Bundle.get("http.table.file.label") + " ");
        partNamePanel.add(label, BorderLayout.WEST);
        this.partTextField = new JTextField();
        partNamePanel.add(this.partTextField, BorderLayout.CENTER);
        filePanel.add(partNamePanel, BorderLayout.NORTH);

        this.fileUploadTable = new FileUploadTable(project);
        this.fileUploadTable.getToolbar().getComponent().setBorder(JBUI.Borders.customLine(UIConstant.BORDER_COLOR, 1, 0, 1, 0));
        filePanel.add(this.fileUploadTable, BorderLayout.CENTER);

        this.fileTabInfo = new TabInfo(filePanel);
        this.fileTabInfo.setText(Bundle.get("http.tab.request.file"));
        this.addTab(this.fileTabInfo);
    }

    @Description("响应体标签页")
    private void responseTab() {
        JPanel respPanel = new JPanel(new BorderLayout(0, 0));
        responseEditor = new CustomEditor(project);
        // responseEditor.setBorder(JBUI.Borders.customLineLeft(UIConstant.EDITOR_BORDER_COLOR));
        respPanel.add(responseEditor, BorderLayout.CENTER);
        respPanel.setBorder(JBUI.Borders.customLineLeft(UIConstant.EDITOR_BORDER_COLOR));
        JPanel respExpandPanel = new JPanel(new BorderLayout(0, 0));
        CommonAction action = new CommonAction(Bundle.get("http.editor.response.action"), "",
                ThemeTool.isDark() ? HttpIcons.General.FULL_SCREEN : HttpIcons.General.FULL_SCREEN_LIGHT);
        action.setAction(e -> new EditorDialog(project, Bundle.get("http.editor.response.action.dialog"),
                responseEditor.getFileType(), responseEditor.getText()).show());
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(action);
        ActionToolbarImpl component = (ActionToolbarImpl) ActionManager.getInstance()
                .createActionToolbar("http.body.editor", group, true).getComponent();
        component.setReservePlaceAutoPopupIcon(false);
        component.setTargetComponent(responseEditor);
        respExpandPanel.add(requestResult, BorderLayout.WEST);
        respExpandPanel.add(component, BorderLayout.EAST);
        respPanel.add(respExpandPanel, BorderLayout.SOUTH);
        responseTabInfo = new TabInfo(respPanel);
        responseTabInfo.setText(Bundle.get("http.tab.request.return"));
        this.addTab(responseTabInfo);
    }

    public void reset() {
        this.headerTable.reloadTableModel();
        this.parameterTable.reloadTableModel();
        this.bodyEditor.setText("");
        this.responseEditor.setText("");
        this.fileUploadTable.reloadTableModel();
        this.partTextField.setText("");
    }

    public void chooseEvent(HttpEnum.HttpMethod httpMethod, HttpEnum.ContentType contentType, @NotNull Map<String, ParamProperty> paramPropertyMap) {
        for (Map.Entry<String, ParamProperty> entry : paramPropertyMap.entrySet()) {
            String k = entry.getKey();
            ParamProperty v = entry.getValue();
            HttpEnum.ParamUsage usage = v.getParamUsage();
            switch (usage) {
                case HEADER -> headerTable.getTableModel().addRow(new String[]{k, v.getDefaultValue() + ""});
                case PATH -> {
                    this.select(parameterTabInfo, true);
                    parameterTable.getTableModel().addRow(new String[]{k, v.getDefaultValue() + ""});
                }
                case URL -> {
                    if (httpMethod.equals(HttpEnum.HttpMethod.POST)) {
                        // 将参数格式化成 username=a&password=a
                        String s = ParamConvert.buildParamPropertyUrlParameters(paramPropertyMap);
                        bodyEditor.setText(s, CustomEditor.TEXT_FILE_TYPE);
                        this.select(bodyTabInfo, true);
                        bodyFileType.setSelectedItem(CustomEditor.TEXT_FILE_TYPE);
                    } else {
                        parameterTable.getTableModel().addRow(new String[]{k, v.getDefaultValue() + ""});
                        this.select(parameterTabInfo, true);
                    }
                }
                case BODY -> {
                    this.select(bodyTabInfo, true);
                    if (contentType.equals(HttpEnum.ContentType.APPLICATION_JSON)) {
                        bodyEditor.setText(v.getDefaultValue().toString(), CustomEditor.JSON_FILE_TYPE);
                        bodyFileType.setSelectedItem(CustomEditor.JSON_FILE_TYPE);
                    } else {
                        bodyEditor.setText(v.getDefaultValue().toString(), CustomEditor.TEXT_FILE_TYPE);
                        bodyFileType.setSelectedItem(CustomEditor.TEXT_FILE_TYPE);
                    }
                }
                case FILE -> {
                    this.select(fileTabInfo, true);
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
    public void resultText(int status) {
        requestResult.setText(CharSequenceUtil.format(REQUEST_RESULT_TEXT, status));
        if (status >= 200 && status < 300) {
            // 成功
            requestResult.setForeground(JBColor.GREEN);
        } else if (status >= 300 && status < 400) {
            // 重定向
            requestResult.setForeground(JBColor.YELLOW);
        } else {
            // 错误
            requestResult.setForeground(JBColor.RED);
        }
    }
}
