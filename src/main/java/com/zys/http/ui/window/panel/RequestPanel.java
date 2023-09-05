package com.zys.http.ui.window.panel;

import com.intellij.openapi.ui.ComboBox;
import jdk.jfr.Description;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jdesktop.swingx.JXButton;

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
public class RequestPanel extends JPanel {

    private TopPart topPart;
    private BottomPart bottomPart;

    public RequestPanel() {
        setLayout(new BorderLayout(0, 0));
        this.topPart = new TopPart();
        this.bottomPart = new BottomPart();
        add(topPart, BorderLayout.NORTH);
        add(bottomPart, BorderLayout.CENTER);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TopPart extends JPanel {

        @Description("请求方式选项")
        private ComboBox<HttpMethod> httpMethodComboBox;

        @Description("IP/HOST文本输入框")
        private JTextField hostTextField;

        private JButton sendRequestBtn;

        @Description("设置的IP/HOST")
        private String hostValue = "";

        public TopPart() {
            setLayout(new GridBagLayout());
            // 初始化所有组件
            initHttpMethodOption();
            initHostOption();
            sendRequestBtn = new JXButton("发送");

            GridBagConstraints gbc = new GridBagConstraints();
            add(httpMethodComboBox, gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.gridwidth = 1;
            add(hostTextField, gbc);
            gbc.weightx = 0;
            gbc.gridx = 2;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            add(sendRequestBtn, gbc);
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
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class BottomPart extends JPanel {
        public BottomPart() {
            add(new JLabel("下半部分"));
        }
    }
}
