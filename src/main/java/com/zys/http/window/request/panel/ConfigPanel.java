package com.zys.http.window.request.panel;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.UIConstant;
import com.zys.http.entity.HttpConfig;
import com.zys.http.entity.ReqHistory;
import com.zys.http.entity.param.ParamProperty;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.topic.HistoryTopic;
import com.zys.http.tool.HttpClient;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.tool.ThreadTool;
import com.zys.http.tool.convert.ParamConvert;
import com.zys.http.tool.ui.ComboBoxTool;
import com.zys.http.tool.ui.DialogTool;
import com.zys.http.ui.tab.RequestTabs;
import com.zys.http.ui.table.FileUploadTable;
import com.zys.http.ui.tree.node.BaseNode;
import com.zys.http.ui.tree.node.MethodNode;
import jdk.jfr.Description;
import org.apache.http.HttpHeaders;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author zhou ys
 * @since 2024-04-16
 */
@Description("API 标签页面-请求配置区域")
final class ConfigPanel extends JBPanel<ConfigPanel> {
    private final transient Project project;
    private ComboBox<HttpEnum.HttpMethod> methodCb;
    private JTextField hostTf;
    private JButton sendRequestBtn;
    private transient RequestTabs requestTabs;

    @Description("缓存每次请求的参数类型")
    private transient Map<String, ParamProperty> paramPropertyMap;

    public ConfigPanel(Project project) {
        super(new BorderLayout(0, 0));
        this.setBorder(JBUI.Borders.customLineTop(UIConstant.BORDER_COLOR));
        this.project = project;
        init();
    }

    private void init() {
        JPanel requestPanel = new JPanel(new BorderLayout(0, 0));
        HttpEnum.HttpMethod[] methods = Arrays.stream(HttpEnum.HttpMethod.values())
                .filter(o -> !o.equals(HttpEnum.HttpMethod.REQUEST))
                .toList().toArray(new HttpEnum.HttpMethod[]{});
        methodCb = new ComboBox<>(methods);
        hostTf = new JTextField();
        sendRequestBtn = new JXButton(Bundle.get("http.api.button.send"));
        initSendRequestEvent();
        requestPanel.add(methodCb, BorderLayout.WEST);
        requestPanel.add(hostTf, BorderLayout.CENTER);
        requestPanel.add(sendRequestBtn, BorderLayout.EAST);
        this.add(requestPanel, BorderLayout.NORTH);

        JPanel tabsPanel = new JPanel(new BorderLayout(0, 0));
        requestTabs = new RequestTabs(project);
        tabsPanel.add(requestTabs, BorderLayout.CENTER);
        this.add(tabsPanel, BorderLayout.CENTER);

        initTopic();
    }

    private void initTopic() {
        project.getMessageBus().connect().subscribe(HistoryTopic.GENERATE_TOPIC, (HistoryTopic.Generate) history -> {
            hostTf.setText(history.getHost() + history.getUri());
            methodCb.setSelectedItem(history.getMethod());
            requestTabs.getFileUploadTable().setModel(history.getFileNames());
            requestTabs.getParameterTable().setModel(history.getParams());
            requestTabs.getHeaderTable().setModel(history.getHeaders());
            requestTabs.getBodyEditor().setText(history.getBody(), HttpClient.parseFileType(history.getContentType()));
            requestTabs.getBodyFileType().setSelectedItem(HttpClient.parseFileType(history.getContentType()));
        });
    }

    private void initSendRequestEvent() {
        sendRequestBtn.addActionListener(event -> {
            HttpEnum.HttpMethod httpMethod = Optional.ofNullable(methodCb.getSelectedItem())
                    .map(HttpEnum.HttpMethod.class::cast).orElse(HttpEnum.HttpMethod.GET);
            String url = hostTf.getText();
            String finalUrl = url;
            Map<String, String> header = requestTabs.getHeaderTable().buildHttpHeader();
            Map<String, String> parameter = requestTabs.getParameterTable().buildHttpHeader();
            String bodyText = requestTabs.getBodyEditor().getText();
            FileUploadTable fileUploadTable = requestTabs.getFileUploadTable();
            String[] fileNames = fileUploadTable.fileNames();
            String partName = requestTabs.getPartTextField().getText();

            if (CharSequenceUtil.isBlank(url)) {
                DialogTool.error(Bundle.get("http.api.button.action.url.empty"));
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
                            DialogTool.error(Bundle.get("http.api.tab.file.action.add.dialog.error"));
                            return;
                        }
                        if (CharSequenceUtil.isEmpty(partName)) {
                            DialogTool.error(Bundle.get("http.api.tab.file.text"));
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
                            saveHistory(finalUrl, header, bodyText, fileNames, httpMethod, responseBody);
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
            this.hostTf.setText("");
            this.methodCb.setSelectedItem(HttpEnum.HttpMethod.GET);
            this.requestTabs.reset();
        }
    }

    void chooseEvent(@NotNull MethodNode methodNode) {
        HttpServiceTool serviceTool = HttpServiceTool.getInstance(project);
        HttpConfig config = serviceTool.getDefaultHttpConfig();
        String protocol = config.getProtocol().name().toLowerCase();
        String configHostValue = config.getHostValue();
        hostTf.setText(protocol + "://" + configHostValue + methodNode.getFragment());
        HttpEnum.HttpMethod httpMethod = methodNode.getValue().getHttpMethod();
        httpMethod = httpMethod.equals(HttpEnum.HttpMethod.REQUEST) ? HttpEnum.HttpMethod.GET : httpMethod;
        methodCb.setSelectedItem(httpMethod);
        // 获取选中节点的参数类型
        NavigatablePsiElement element = methodNode.getValue().getPsiElement();
        HttpEnum.HttpMethod finalHttpMethod = httpMethod;

        ReadAction.nonBlocking(() -> element instanceof PsiMethod psiMethod ?
                        ParamConvert.parsePsiMethodParams(psiMethod, true) : ParamConvert.parseFunctionParams((KtNamedFunction) element, true)
                ).finishOnUiThread(ModalityState.defaultModalityState(), map -> {
                    paramPropertyMap = map;
                    this.requestTabs.reset();
                    HttpEnum.ContentType type = (HttpEnum.ContentType) map.get(ParamConvert.REQUEST_TYPE_KEY).getDefaultValue();
                    paramPropertyMap.remove(ParamConvert.REQUEST_TYPE_KEY);
                    this.requestTabs.chooseEvent(finalHttpMethod, type, paramPropertyMap);
                })
                .submit(ThreadTool.getExecutor());
    }

    private void saveHistory(String finalUrl, Map<String, String> header, String bodyText, String[] fileNames, HttpEnum.HttpMethod httpMethod, String resBody) {
        // 获取到当前环境的配置
        HttpServiceTool serviceTool = HttpServiceTool.getInstance(project);
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
        project.getMessageBus().syncPublisher(HistoryTopic.CHANGE_TOPIC).save(history);
    }
}
