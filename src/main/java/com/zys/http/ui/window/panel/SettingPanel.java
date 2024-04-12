package com.zys.http.ui.window.panel;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.topic.RefreshTreeTopic;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.tool.ui.DialogTool;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author zhou ys
 * @since 2024-04-09
 */
@Deprecated(since = "1.6.1")
public class SettingPanel extends JBPanel<SettingPanel> {
    private final transient Project project;
    private final JTextField annoTextField;
    private final transient HttpServiceTool serviceTool;


    public SettingPanel(Project project) {
        super(new BorderLayout(0, 0));
        this.project = project;
        this.serviceTool = HttpServiceTool.getInstance(project);
        this.annoTextField = new JTextField(50);
        init();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                transferFocus();
            }
        });
    }

    private void init() {
        JPanel main = new JPanel(new BorderLayout(0, 0));

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JBCheckBox defaultBox = generateDefaultBox();
        gbc.gridy = 0;
        panel.add(defaultBox, gbc);


        JBCheckBox vcsBox = refreshWhenVcsChangeBox();
        gbc.gridy = 1;
        panel.add(vcsBox, gbc);

        JBCheckBox seBox = enableSearchEverywhereBox();
        gbc.gridy = 2;

        panel.add(seBox, gbc);

        gbc.gridy = 3;
        panel.add(customAnnoPanel(), gbc);

        main.add(panel, BorderLayout.WEST);
        add(main, BorderLayout.NORTH);


    }

    private @NotNull JBCheckBox generateDefaultBox() {
        JBCheckBox defaultBox = new JBCheckBox(Bundle.get("http.api.icon.setting.action.default.env"));
        defaultBox.setSelected(serviceTool.getGenerateDefault());
        defaultBox.addItemListener(e -> {
            serviceTool.refreshGenerateDefault();
            ApplicationManager.getApplication().invokeLater(() -> {
                defaultBox.setSelected(serviceTool.getGenerateDefault());
                project.getMessageBus().syncPublisher(RefreshTreeTopic.TOPIC).refresh(false);
            });
        });
        return defaultBox;
    }


    private @NotNull JBCheckBox refreshWhenVcsChangeBox() {
        JBCheckBox vcsBox = new JBCheckBox(Bundle.get("http.api.icon.setting.action.vcs.change"));
        vcsBox.setSelected(serviceTool.getRefreshWhenVcsChange());
        vcsBox.addItemListener(e -> {
            serviceTool.refreshWhenVcsChange();
            ApplicationManager.getApplication().invokeLater(() -> vcsBox.setSelected(serviceTool.getRefreshWhenVcsChange()));
        });
        return vcsBox;
    }

    private @NotNull JBCheckBox enableSearchEverywhereBox() {
        JBCheckBox seBox = new JBCheckBox(Bundle.get("http.api.icon.setting.action.search.everywhere"));
        seBox.setSelected(serviceTool.getEnableSearchEverywhere());
        seBox.addItemListener(e -> {
            serviceTool.refreshEnableSearchEverywhere();
            ApplicationManager.getApplication().invokeLater(() -> seBox.setSelected(serviceTool.getEnableSearchEverywhere()));
        });
        return seBox;
    }

    private @NotNull JPanel customAnnoPanel() {
        String customAnno = serviceTool.getCustomAnno();

        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.add(new JLabel(Bundle.get("http.setting.custom.anno") + " "), BorderLayout.WEST);

        if (CharSequenceUtil.isNotBlank(customAnno)) {
            annoTextField.setText(customAnno);
        }
        panel.add(annoTextField, BorderLayout.CENTER);

        annoTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    transferFocus();
                }
            }
        });
        annoTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String text = annoTextField.getText();
                if (CharSequenceUtil.isNotBlank(text) && !text.contains(".") && !text.startsWith(".") && !text.endsWith(".")) {
                    DialogTool.error("错误的注解全限名");
                    ApplicationManager.getApplication().invokeLater(() -> {
                        serviceTool.setCustomAnno("");
                        annoTextField.setText("");
                    });
                    return;
                }
                serviceTool.setCustomAnno(text);
                project.getMessageBus().syncPublisher(RefreshTreeTopic.TOPIC).refresh(false);
            }
        });
        return panel;
    }
}
