package com.zys.http.extension.setting;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.zys.http.extension.service.Bundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

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

    public void reset(boolean generateDefault, boolean refreshWhenVcsChange, boolean enableSearchEverywhere) {
        defaultBox.setSelected(generateDefault);
        vcsBox.setSelected(refreshWhenVcsChange);
        seBox.setSelected(enableSearchEverywhere);
    }

    public boolean getGenerateDefault() {
        return defaultBox.isSelected();
    }

    public boolean getRefreshWhenVcsChange() {
        return vcsBox.isSelected();
    }

    public boolean getEnableSearchEverywhere() {
        return seBox.isSelected();
    }

    public String getCustomControllerAnnotation() {
        return annoTextField.getText();
    }


    private @NotNull JPanel customAnnoPanel() {
        String customAnno = httpSetting.getCustomAnno();

        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.add(new JLabel(Bundle.get("http.extension.setting.custom.controller.annotation") + " "), BorderLayout.WEST);

        if (CharSequenceUtil.isNotBlank(customAnno)) {
            annoTextField.setText(customAnno);
        }
        panel.add(annoTextField, BorderLayout.CENTER);

        return panel;
    }
}