package com.zys.http.ui.window.panel;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.Header;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.JBSplitter;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.HttpEnum.HttpMethod;
import com.zys.http.constant.UIConstant;
import com.zys.http.entity.HttpConfig;
import com.zys.http.entity.ReqHistory;
import com.zys.http.entity.param.ParamProperty;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.topic.HisListChangeTopic;
import com.zys.http.tool.*;
import com.zys.http.tool.convert.ParamConvert;
import com.zys.http.tool.ui.ComboBoxTool;
import com.zys.http.tool.ui.DialogTool;
import com.zys.http.ui.tab.RequestTabs;
import com.zys.http.ui.table.FileUploadTable;
import com.zys.http.ui.tree.HttpApiTreePanel;
import com.zys.http.ui.tree.node.BaseNode;
import com.zys.http.ui.tree.node.MethodNode;
import jdk.jfr.Description;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.http.HttpHeaders;
import org.jdesktop.swingx.JXButton;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author zys
 * @since 2023-08-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RequestPanel extends JBSplitter {

    @Getter
    private final transient Project project;
    private final transient HistoryTool historyTool;
    @Getter
    private final transient HttpServiceTool serviceTool;


    // ================== 上半部分的组件 ==================
    @Description("树形结构列表")
    private HttpApiTreePanel httpApiTreePanel;

    // ================== 下半部分的组件 ==================
    @Description("请求方式选项")
    private ComboBox<HttpMethod> httpMethodComboBox;
    @Description("IP/HOST文本输入框")
    private JTextField hostTextField;
    @Description("发起请求的按钮")
    private JButton sendRequestBtn;
    @Description("标签栏")
    private transient RequestTabs requestTabs;

    @Description("缓存每次请求的参数类型")
    private transient Map<String, ParamProperty> paramPropertyMap;

    public RequestPanel(Project project) {
        super(true, Window.class.getName(), 0.5F);
        this.project = project;
        this.serviceTool = HttpServiceTool.getInstance(project);
        this.historyTool = HistoryTool.getInstance(project);
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
        requestTabs = new RequestTabs(project);
        tabsPanel.add(requestTabs, BorderLayout.CENTER);
        secondPanel.add(tabsPanel, BorderLayout.CENTER);
        this.setSecondComponent(secondPanel);
    }

    @Description("初始化发送请求按钮")
    private void initSendRequestEvent() {
        sendRequestBtn.addActionListener(event -> {
            HttpMethod httpMethod = Optional.ofNullable(httpMethodComboBox.getSelectedItem())
                    .map(HttpMethod.class::cast).orElse(HttpMethod.GET);
            String url = hostTextField.getText();
            String finalUrl = url;
            Map<String, String> header = requestTabs.getHeaderTable().buildHttpHeader();
            Map<String, String> parameter = requestTabs.getParameterTable().buildHttpHeader();
            String bodyText = requestTabs.getBodyEditor().getText();
            FileUploadTable fileUploadTable = requestTabs.getFileUploadTable();
            String[] fileNames = fileUploadTable.fileNames();
            String partName = requestTabs.getPartTextField().getText();

            if (CharSequenceUtil.isBlank(url)) {
                DialogTool.error(Bundle.get("http.dialog.error.no.selected"));
                return;
            }
            makeSureParamPropertyMapNotNull();
            for (Map.Entry<String, ParamProperty> entry : paramPropertyMap.entrySet()) {
                String k = entry.getKey();
                ParamProperty v = entry.getValue();
                switch (v.getParamUsage()) {
                    case PATH -> url = url.replace("{" + k + "}", parameter.remove(k));
                    case FILE -> {
                        if (fileUploadTable.getValueTable().getRowCount() < 0) {
                            DialogTool.error(Bundle.get("http.table.file.dialog.error"));
                            return;
                        }
                        if (CharSequenceUtil.isEmpty(partName)) {
                            DialogTool.error(Bundle.get("http.table.file.text"));
                            return;
                        }
                    }
                    default -> {// 不处理
                    }
                }
            }

            requestTabs.select(requestTabs.getResponseTabInfo(), true);
            requestTabs.getResponseEditor().setText("");
            requestTabs.getRequestResult().setText("");


            HttpClient.run(
                    HttpClient.newRequest(httpMethod, url, header, parameter, bodyText, partName, fileNames),
                    response -> {
                        final FileType fileType = HttpClient.parseFileType(response);
                        final String responseBody = response.body();
                        ApplicationManager.getApplication().invokeLater(() -> {
                            this.requestTabs.getResponseEditor().setText(responseBody, fileType);
                            this.requestTabs.resultText(response.getStatus());
                            saveHistory(finalUrl, header, bodyText, fileNames, httpMethod, response.header(Header.CONTENT_TYPE), responseBody);
                        });
                    },
                    e -> {
                        final String response = String.format("%s", e);
                        ApplicationManager.getApplication().invokeLater(
                                () -> {
                                    this.requestTabs.getResponseEditor().setText(response, ComboBoxTool.TEXT_FILE_TYPE);
                                    this.requestTabs.resultText(404);
                                }
                        );
                    },
                    ms -> ApplicationManager.getApplication().invokeLater(() ->
                            this.requestTabs.getRequestResult().setText(this.requestTabs.getRequestResult().getText() + ", " + ms + "ms"))
            );
        });
    }


    private void makeSureParamPropertyMapNotNull() {
        paramPropertyMap = Objects.isNull(paramPropertyMap) ? new HashMap<>() : paramPropertyMap;
    }

    public void reload(BaseNode<?> chooseNode) {
        if (Objects.nonNull(chooseNode) && chooseNode instanceof MethodNode m) {
            this.requestTabs.getHeaderTable().reloadTableModel();
            chooseEvent(m);
        } else {
            this.hostTextField.setText("");
            this.httpMethodComboBox.setSelectedItem(HttpMethod.GET);
            this.requestTabs.reset();
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
        HttpMethod finalHttpMethod = httpMethod;
        ReadAction.nonBlocking(() -> {
            HttpEnum.ContentType contentType = PsiTool.Class.contentTypeHeader((PsiClass) psiMethod.getParent());
            return PsiTool.Method.contentType(contentType, psiMethod);
        }).finishOnUiThread(ModalityState.defaultModalityState(), type -> {
            this.requestTabs.getHeaderTable().addContentType(type.getValue());
            // 填充参数
            // 先清空 model
            ReadAction.nonBlocking(() -> ParamConvert.parsePsiMethodParams(psiMethod, true))
                    .finishOnUiThread(ModalityState.defaultModalityState(), map -> {
                        paramPropertyMap = map;
                        this.requestTabs.reset();
                        this.requestTabs.chooseEvent(finalHttpMethod, type, paramPropertyMap);
                    })
                    .submit(ThreadTool.getExecutor());
        }).submit(ThreadTool.getExecutor());
    }

    private void saveHistory(String finalUrl, Map<String, String> header, String bodyText, String[] fileNames, HttpEnum.HttpMethod httpMethod, String resHeader, String resBody) {
        // 获取到当前环境的配置
        HttpConfig httpConfig = serviceTool.getHttpConfig(serviceTool.getSelectedEnv());
        if (Objects.isNull(httpConfig)) {
            httpConfig = HttpServiceTool.DEFAULT_HTTP_CONFIG;
        }
        String host = httpConfig.getProtocol().name().toLowerCase() + "://" + httpConfig.getHostValue();
        String uri = finalUrl.substring(host.length());

        ReqHistory history = new ReqHistory();
        history.setUri(uri);
        history.setHost(host);
        history.setHeaders(requestTabs.getHeaderTable().buildHttpHeader());
        String contentType = header.getOrDefault(HttpHeaders.CONTENT_TYPE, HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED.getValue());
        history.setContentType(contentType);
        history.setBody(bodyText);
        history.setFileNames(fileNames);
        history.setParams(requestTabs.getParameterTable().buildHttpHeader());
        history.setMethod(httpMethod);
        history.setRes(resBody);
        history.setTime(DatePattern.NORM_DATETIME_FORMATTER.format(LocalDateTime.now()));
        project.getMessageBus().syncPublisher(HisListChangeTopic.TOPIC).save(history);
    }
}
