package com.zys.http.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.SeparatorComponent;
import com.intellij.ui.SeparatorOrientation;
import com.intellij.util.ui.JBUI;
import com.zys.http.entity.HttpConfig;
import com.zys.http.ui.table.EnvAddOrEditTable;
import com.zys.http.ui.table.EnvShowTable;
import com.zys.http.tool.HttpPropertyTool;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

    @Description("配置名称")
    private JTextField configNameTF;

    @Description("IP:PORT/域名")
    private JTextField hostTF;

    @Description("协议选择框")
    private ComboBox<Protocol> protocolCB;

    @Description("数据表格")
    private final EnvAddOrEditTable envAddOrEditTable;

    @Description("添加/修改后方便刷新数据")
    private final EnvShowTable envShowTable;

    @Description("是否是添加")
    private final boolean isAdd;

    public EnvAddOrEditDialog(@NotNull Project project, boolean isAdd, String selectEnv, EnvShowTable envShowTable) {
        super(project, true);
        envAddOrEditTable = new EnvAddOrEditTable(project, isAdd, selectEnv);
        this.envShowTable = envShowTable;
        this.isAdd = isAdd;
        init();
        getRootPane().setMinimumSize(new Dimension(500, 400));
        setTitle(isAdd ? "添加环境配置" : "修改环境配置");
        setCancelButtonText("取消");
        setOKButtonText("确定");
        setAutoAdjustable(true);
        if (!isAdd) {
            // 修改时配置名称禁止修改
            configNameTF.setText(selectEnv);
            configNameTF.setEnabled(false);
            configNameTF.setDisabledTextColor(JBColor.BLACK);
            HttpPropertyTool httpPropertyTool = envAddOrEditTable.getHttpPropertyTool();
            HttpConfig httpConfig = httpPropertyTool.getHttpConfig(selectEnv);
            hostTF.setText(httpConfig.getHostValue());
            protocolCB.setSelectedItem(httpConfig.getProtocol());
        }
    }

    @Override
    protected @NotNull JComponent createCenterPanel() {
        JPanel main = new JPanel(new BorderLayout());
        // 配置名字
        JPanel first = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insetsBottom(4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        first.add(new JLabel("环境名称:  "), gbc);
        gbc.gridy = 1;
        first.add(new JLabel("协       议:  "), gbc);
        gbc.gridy = 2;
        first.add(new JLabel("IP/HOST:  "), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        configNameTF = new JTextField();
        configNameTF.setToolTipText("输入配置名");
        first.add(configNameTF, gbc);

        // 环境配置
        gbc.gridy = 1;
        protocolCB = new ComboBox<>(Protocol.values());
        protocolCB.setSelectedItem(Protocol.HTTP);
        first.add(protocolCB, gbc);

        // IP/HOST
        gbc.gridy = 2;
        hostTF = new JTextField();
        hostTF.setToolTipText("输入 ip:port 或域名");
        first.add(hostTF, gbc);

        // 请求头分割线
        gbc.gridy = 3;
        gbc.gridx = 0;
        first.add(headerPanel(), gbc);

        // 表格
        gbc.gridy = 4;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = JBUI.emptyInsets();
        first.add(envAddOrEditTable, gbc);
        main.add(first, BorderLayout.NORTH);

        return main;
    }

    private JPanel headerPanel() {
        JPanel header = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        header.add(new JLabel("请求头 "), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        SeparatorComponent separator = new SeparatorComponent(new JBColor(JBColor.darkGray, new Color(57, 59, 64)),
                SeparatorOrientation.HORIZONTAL);
        header.add(separator, gbc);
        header.add(new JPanel());
        return header;
    }

    @Override
    protected void doOKAction() {
        HttpPropertyTool httpPropertyTool = envAddOrEditTable.getHttpPropertyTool();
        String configName = configNameTF.getText();
        // 添加时需要检测是否存在
        if (httpPropertyTool.getHttpConfig(configName) != null && envAddOrEditTable.isAdd()) {
            ErrorDialog.show("当前环境配置名已存在");
            return;
        }
        String host = hostTF.getText();
        Protocol protocol = (Protocol) protocolCB.getSelectedItem();
        protocol = Objects.isNull(protocol) ? Protocol.HTTP : protocol;
        Map<String, String> header = envAddOrEditTable.buildHttpHeader();

        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setHeaders(header);
        httpConfig.setHostValue(host);
        httpConfig.setProtocol((Protocol) protocolCB.getSelectedItem());

        httpPropertyTool.putHttpConfig(configName, httpConfig);
        if (Objects.nonNull(envShowTable)){
            DefaultTableModel model = (DefaultTableModel) envShowTable.getValueTable().getModel();
            if (isAdd) {
                model.addRow(new String[]{configName, protocol.toString(), host});
            } else {
                int selectedRow = envShowTable.getValueTable().getSelectedRow();
                model.setValueAt(protocol.toString(), selectedRow, 1);
                model.setValueAt(host, selectedRow, 2);
            }
        }
        super.doOKAction();
    }
}