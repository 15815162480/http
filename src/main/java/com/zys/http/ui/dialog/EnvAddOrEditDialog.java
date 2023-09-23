package com.zys.http.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.SeparatorComponent;
import com.intellij.ui.SeparatorOrientation;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.UIConstant;
import com.zys.http.entity.HttpConfig;
import com.zys.http.service.Bundle;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.tool.ui.DialogTool;
import com.zys.http.ui.table.EnvHeaderTable;
import jdk.jfr.Description;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import static com.zys.http.constant.HttpEnum.Protocol;

/**
 * @author zys
 * @since 2023-08-19
 */
@Description("添加或修改环境配置的对话框")
public class EnvAddOrEditDialog extends DialogWrapper {

    @Description("是否是添加")
    private final boolean isAdd;

    @Description("环境配置工具类")
    private final HttpServiceTool serviceTool;

    @Description("配置名称")
    private JTextField configNameTF;
    @Description("IP:PORT/域名")
    private JTextField hostTF;
    @Description("协议选择框")
    private ComboBox<Protocol> protocolCB;

    @Description("数据表格")
    private final EnvHeaderTable envAddOrEditTable;

    @Setter
    @Description("添加的回调, <新增的环境配置名, 环境配置>")
    private BiConsumer<String, HttpConfig> addCallback;

    @Setter
    @Description("添加的回调, <新增的环境配置名, 环境配置>")
    private BiConsumer<String, HttpConfig> editCallback;

    public EnvAddOrEditDialog(Project project, boolean isAdd, String selectEnv) {
        super(project, true);
        serviceTool = HttpServiceTool.getInstance(project);
        envAddOrEditTable = new EnvHeaderTable(project, isAdd);

        this.isAdd = isAdd;
        init();
        getRootPane().setMinimumSize(new Dimension(500, 400));
        setTitle(isAdd ? Bundle.get("http.dialog.add.env.config") : Bundle.get("http.dialog.edit.env.config"));
        setCancelButtonText(Bundle.get("http.text.cancel"));
        setOKButtonText(Bundle.get("http.text.ok"));
        setAutoAdjustable(true);
        if (!isAdd) {
            // 修改时配置名称禁止修改
            configNameTF.setText(selectEnv);
            configNameTF.setEnabled(false);
            configNameTF.setDisabledTextColor(JBColor.BLACK);
            HttpConfig httpConfig = serviceTool.getHttpConfig(selectEnv);
            hostTF.setText(httpConfig.getHostValue());
            protocolCB.setSelectedItem(httpConfig.getProtocol());
        }
    }

    @Override
    protected @NotNull JComponent createCenterPanel() {
        JPanel main = new JPanel(new BorderLayout(0, 0));
        // 配置名字
        JPanel first = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insetsBottom(4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        first.add(new JLabel(Bundle.get("http.dialog.env.config.name")), gbc);
        gbc.gridy = 1;
        first.add(new JLabel(Bundle.get("http.dialog.env.config.protocol")), gbc);
        gbc.gridy = 2;
        first.add(new JLabel(Bundle.get("http.dialog.env.config.ip")), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        configNameTF = new JTextField();
        configNameTF.setToolTipText(Bundle.get("http.dialog.env.config.name.tooltip"));
        first.add(configNameTF, gbc);

        // 环境配置
        gbc.gridy = 1;
        protocolCB = new ComboBox<>(Protocol.values());
        protocolCB.setSelectedItem(Protocol.HTTP);
        first.add(protocolCB, gbc);

        // IP/HOST
        gbc.gridy = 2;
        hostTF = new JTextField();
        hostTF.setToolTipText(Bundle.get("http.dialog.env.config.ip.tooltip"));
        first.add(hostTF, gbc);

        // 请求头分割线
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.insets = JBUI.insetsBottom(5);
        first.add(headerPanel(), gbc);

        // 表格
        main.add(first, BorderLayout.NORTH);
        main.add(envAddOrEditTable, BorderLayout.CENTER);

        return main;
    }

    private JPanel headerPanel() {
        JPanel header = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        header.add(new JLabel(Bundle.get("http.dialog.env.separator.header") + " "), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        SeparatorComponent separator = new SeparatorComponent(UIConstant.BORDER_COLOR, SeparatorOrientation.HORIZONTAL);
        header.add(separator, gbc);
        return header;
    }

    @Override
    protected void doOKAction() {
        String configName = configNameTF.getText();
        // 添加时需要检测是否存在
        if (serviceTool.getHttpConfig(configName) != null && envAddOrEditTable.isAdd()) {
            DialogTool.error(Bundle.get("http.dialog.env.config.existed"));
            return;
        }
        String host = hostTF.getText();
        Protocol protocol = (Protocol) protocolCB.getSelectedItem();
        protocol = Objects.isNull(protocol) ? Protocol.HTTP : protocol;
        Map<String, String> header = envAddOrEditTable.buildHttpHeader();

        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setHeaders(header);
        httpConfig.setHostValue(host);
        httpConfig.setProtocol(protocol);

        serviceTool.putHttpConfig(configName, httpConfig);
        if (isAdd) {
            addCallback.accept(configName, httpConfig);
        } else {
            editCallback.accept(configName, httpConfig);
        }
        super.doOKAction();
    }
}