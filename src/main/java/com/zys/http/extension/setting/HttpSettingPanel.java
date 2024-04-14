package com.zys.http.extension.setting;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.topic.RefreshTreeTopic;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author zys
 * @since 2024-04-13
 */
public class HttpSettingPanel extends JBPanel<HttpSettingPanel> {
    private final transient HttpSetting httpSetting;
    private final JBCheckBox defaultBox = new JBCheckBox(Bundle.get("http.extension.setting.default.env"));
    private final JBCheckBox vcsBox = new JBCheckBox(Bundle.get("http.extension.setting.vcs.change"));
    private final JBCheckBox seBox = new JBCheckBox(Bundle.get("http.extension.setting.search.everywhere"));
    private final JTextField annoTextField = new JTextField(30);

    public HttpSettingPanel() {
        super(new BorderLayout(0, 0));
        httpSetting = HttpSetting.getInstance();
        init();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                transferFocus();
            }
        });
    }

    public void init() {
        defaultBox.setSelected(httpSetting.getGenerateDefault());
        defaultBox.addChangeListener(l -> httpSetting.setGenerateDefault(defaultBox.isSelected()));
        vcsBox.setSelected(httpSetting.getRefreshWhenVcsChange());
        vcsBox.addChangeListener(l -> httpSetting.setRefreshWhenVcsChange(vcsBox.isSelected()));

        seBox.setSelected(httpSetting.getEnableSearchEverywhere());
        seBox.addChangeListener(l -> httpSetting.setEnableSearchEverywhere(seBox.isSelected()));

        JPanel main = new JPanel(new BorderLayout(0, 0));
        JPanel northPanel = new JPanel(new BorderLayout(0, 0));

        JPanel checkBoxPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insetsBottom(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        checkBoxPanel.add(defaultBox, gbc);

        gbc.gridy = 1;
        checkBoxPanel.add(vcsBox, gbc);

        gbc.gridy = 2;
        checkBoxPanel.add(seBox, gbc);
        gbc.gridx = 1;
        checkBoxPanel.add(new JPanel(new BorderLayout(0, 0)), gbc);
        northPanel.add(checkBoxPanel, BorderLayout.WEST);
        main.add(northPanel, BorderLayout.NORTH);
        main.add(customAnnoPanel(), BorderLayout.CENTER);
        add(main, BorderLayout.NORTH);
    }

    public void reset(boolean generateDefault, boolean refreshWhenVcsChange, boolean enableSearchEverywhere, String oldCustomAnno) {
        defaultBox.setSelected(generateDefault);
        vcsBox.setSelected(refreshWhenVcsChange);
        seBox.setSelected(enableSearchEverywhere);
        annoTextField.setText(oldCustomAnno);
    }


    private @NotNull JPanel customAnnoPanel() {
        String customAnno = httpSetting.getCustomAnno();

        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.add(new JLabel(Bundle.get("http.extension.setting.custom.controller.annotation") + " "), BorderLayout.WEST);

        if (CharSequenceUtil.isNotBlank(customAnno)) {
            annoTextField.setText(customAnno);
        }


        annoTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String text = annoTextField.getText();
                if (CharSequenceUtil.isBlank(text)) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        annoTextField.setText("");
                        httpSetting.setCustomAnno("");
                    });
                    return;
                }
                httpSetting.setCustomAnno(text);
                @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
                for (Project project : openProjects) {
                    project.getMessageBus().syncPublisher(RefreshTreeTopic.TOPIC).refresh(false);
                }
            }
        });

        panel.add(annoTextField, BorderLayout.CENTER);

        return panel;
    }
}