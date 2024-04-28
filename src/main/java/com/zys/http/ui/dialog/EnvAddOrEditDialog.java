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
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.topic.EnvironmentTopic;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.tool.ui.ComboBoxTool;
import com.zys.http.tool.ui.DialogTool;
import com.zys.http.ui.table.EnvHeaderTable;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Objects;

import static com.zys.http.constant.HttpEnum.Protocol;

/**
 * @author zys
 * @since 2023-08-19
 */
@Description("添加或修改环境配置的对话框")
public class EnvAddOrEditDialog extends DialogWrapper {
    @Description("是否是添加")
    private final boolean isAdd;
    @Description("项目对象")
    private final Project project;
    @Description("环境配置工具类")
    private final HttpServiceTool serviceTool;
    @Description("数据表格")
    private final EnvHeaderTable envAddOrEditTable;
    @Description("配置名称")
    private JTextField configNameTF;
    @Description("IP:PORT/域名")
    private JTextField hostTF;
    @Description("协议选择框")
    private ComboBox<Protocol> protocolCB;

    public EnvAddOrEditDialog(Project project, boolean isAdd, String selectEnv) {
        super(project, true);
        this.project = project;
        this.serviceTool = HttpServiceTool.getInstance(project);
        this.envAddOrEditTable = new EnvHeaderTable(project, isAdd, selectEnv);

        this.isAdd = isAdd;
        init();
        getRootPane().setMinimumSize(new Dimension(500, 400));
        setTitle(isAdd ? Bundle.get("http.env.icon.add.dialog") : Bundle.get("http.env.icon.edit.dialog"));
        setCancelButtonText(Bundle.get("http.common.dialog.action.cancel"));
        setOKButtonText(Bundle.get("http.common.dialog.action.ok"));
        setAutoAdjustable(true);
        if (!isAdd) {
            // 修改时配置名称禁止修改
            this.configNameTF.setText(selectEnv);
            this.configNameTF.setEnabled(false);
            this.configNameTF.setDisabledTextColor(JBColor.BLACK);
            HttpConfig httpConfig = serviceTool.getHttpConfig(selectEnv);
            this.hostTF.setText(httpConfig.getHostValue());
            this.protocolCB.setSelectedItem(httpConfig.getProtocol());
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
        first.add(new JLabel(Bundle.get("http.env.action.add.dialog.env.name")), gbc);
        gbc.gridy = 1;
        first.add(new JLabel(Bundle.get("http.env.action.add.dialog.protocol")), gbc);
        gbc.gridy = 2;
        first.add(new JLabel(Bundle.get("http.env.action.add.dialog.ip")), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        configNameTF = new JTextField();
        configNameTF.setToolTipText(Bundle.get("http.env.action.add.dialog.name.check"));
        first.add(configNameTF, gbc);

        // 环境配置
        gbc.gridy = 1;
        protocolCB = ComboBoxTool.protocolComboBox();
        protocolCB.setSelectedItem(Protocol.HTTP);
        first.add(protocolCB, gbc);

        // IP/HOST
        gbc.gridy = 2;
        hostTF = new JTextField();
        hostTF.setToolTipText(Bundle.get("http.env.action.add.dialog.ip.check"));
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
        header.add(new JLabel(Bundle.get("http.env.action.add.dialog.header.separator") + " "), gbc);
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
            DialogTool.error(Bundle.get("http.env.action.add.dialog.name.existed"));
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

        if (isAdd) {
            project.getMessageBus().syncPublisher(EnvironmentTopic.LIST_TOPIC).save(configName, httpConfig);
        } else {
            project.getMessageBus().syncPublisher(EnvironmentTopic.LIST_TOPIC).edit(configName, httpConfig);
        }
        super.doOKAction();
    }
}