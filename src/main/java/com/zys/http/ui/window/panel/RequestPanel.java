package com.zys.http.ui.window.panel;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBSplitter;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.UIConstant;
import com.zys.http.ui.tree.HttpApiTreePanel;
import jdk.jfr.Description;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import static com.zys.http.constant.HttpEnum.HttpMethod;

/**
 * @author zys
 * @since 2023-08-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RequestPanel extends JBSplitter {

    @Description("请求方式选项")
    private ComboBox<HttpMethod> httpMethodComboBox;

    @Description("IP/HOST文本输入框")
    private JTextField hostTextField;

    private JButton sendRequestBtn;

    @Description("设置的IP/HOST")
    private String hostValue = "";

    @Description("树形结构列表")
    private HttpApiTreePanel httpApiTreePanel;

    private BottomPart bottomPart;
    private final transient Project project;

    public RequestPanel(@NotNull Project project) {
        super(true, Window.class.getName(), 0.6F);
        this.project = project;
        initFirstPanel();
        this.bottomPart = new BottomPart();
        this.setSecondComponent(bottomPart);
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
            hostTextField.setText(methodNode.getFragment());
            httpMethodComboBox.setSelectedItem(methodNode.getValue().getHttpMethod());
        });
    }

    @Description("请求方式选项")
    private void initHttpMethodOption() {
        httpMethodComboBox = new ComboBox<>(HttpMethod.values());
        httpMethodComboBox.setSelectedItem(HttpMethod.GET);
        httpMethodComboBox.setFocusable(false);
        httpMethodComboBox.addActionListener(e -> {
            Object selectedItem = httpMethodComboBox.getSelectedItem();
            System.out.println(selectedItem);
        });
    }

    @Description("host选项")
    private void initHostOption() {
        hostTextField = new JTextField();
        hostTextField.setColumns(10);
        hostTextField.setText(hostValue);
        hostTextField.addActionListener(e -> hostValue = hostTextField.getText());
        hostTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                hostValue = hostTextField.getText();
            }
        });
    }


    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class BottomPart extends JPanel {
        public BottomPart() {
            add(new JLabel("下半部分"));
        }
    }
}
