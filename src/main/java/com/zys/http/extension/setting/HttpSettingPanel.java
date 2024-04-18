package com.zys.http.extension.setting;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.NumberDocument;
import com.intellij.ui.SeparatorComponent;
import com.intellij.ui.SeparatorOrientation;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.UIConstant;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.topic.TreeTopic;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
    private final JTextField timeTf = new JTextField(20);

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
        // region 基础设置
        JPanel basicSettingPanel = new JPanel(new BorderLayout(0, 0));
        JPanel basicOptionPanel = createOptionPanel(Bundle.get("http.extension.setting.option.base"));
        basicSettingPanel.add(basicOptionPanel, BorderLayout.NORTH);

        JPanel basicContentPanel = new JPanel(new BorderLayout(0, 0));

        JPanel tPanel = new JPanel(new BorderLayout(0, 0));
        JPanel checkBoxPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        defaultBox.setSelected(httpSetting.getGenerateDefault());
        defaultBox.addChangeListener(l -> httpSetting.setGenerateDefault(defaultBox.isSelected()));
        checkBoxPanel.add(defaultBox, gbc);

        gbc.gridy = 1;
        vcsBox.setSelected(httpSetting.getRefreshWhenVcsChange());
        vcsBox.addChangeListener(l -> httpSetting.setRefreshWhenVcsChange(vcsBox.isSelected()));
        checkBoxPanel.add(vcsBox, gbc);

        gbc.gridy = 2;
        seBox.setSelected(httpSetting.getEnableSearchEverywhere());
        seBox.addChangeListener(l -> httpSetting.setEnableSearchEverywhere(seBox.isSelected()));
        checkBoxPanel.add(seBox, gbc);
        gbc.gridx = 1;
        checkBoxPanel.add(new JPanel(new BorderLayout(0, 0)), gbc);
        tPanel.add(checkBoxPanel, BorderLayout.WEST);

        basicContentPanel.add(tPanel, BorderLayout.NORTH);
        basicContentPanel.add(customAnnoPanel(), BorderLayout.CENTER);
        basicSettingPanel.add(basicContentPanel, BorderLayout.CENTER);
        // endregion

        JPanel requestSettingPanel = new JPanel(new BorderLayout(0, 10));
        JPanel requestOptionPanel = createOptionPanel(Bundle.get("http.extension.setting.option.request"));
        requestSettingPanel.add(requestOptionPanel, BorderLayout.NORTH);
        JPanel requestContentPanel = new JPanel(new BorderLayout(0, 0));
        JPanel requestTimeoutPanel = new JPanel(new BorderLayout(0, 0));
        requestTimeoutPanel.add(new JLabel(Bundle.get("http.extension.setting.option.request.timeout") + " "), BorderLayout.WEST);
        timeTf.setDocument(new NumberDocument());
        timeTf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String text = timeTf.getText();
                if (CharSequenceUtil.isBlank(text)) {
                    ApplicationManager.getApplication().invokeLater(() -> httpSetting.setTimeout(HttpSetting.DEFAULT_TIMEOUT + ""));
                    return;
                }
                httpSetting.setTimeout(text);
            }
        });
        requestTimeoutPanel.add(timeTf, BorderLayout.CENTER);
        requestContentPanel.add(requestTimeoutPanel, BorderLayout.WEST);
        requestSettingPanel.add(requestContentPanel, BorderLayout.CENTER);


        JPanel main = new JPanel(new BorderLayout(0, 10));
        main.add(basicSettingPanel, BorderLayout.NORTH);
        main.add(requestSettingPanel, BorderLayout.CENTER);
        add(main, BorderLayout.NORTH);
    }

    public void reset(boolean generateDefault, boolean refreshWhenVcsChange, boolean enableSearchEverywhere, String oldCustomAnno, long oldTimeout) {
        defaultBox.setSelected(generateDefault);
        vcsBox.setSelected(refreshWhenVcsChange);
        seBox.setSelected(enableSearchEverywhere);
        annoTextField.setText(oldCustomAnno);
        timeTf.setText(oldTimeout + "");
    }

    private @NotNull JPanel createOptionPanel(String option) {
        JPanel optionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        JLabel jLabel = new JLabel(option + " ");
        optionPanel.add(jLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        SeparatorComponent separator = new SeparatorComponent(UIConstant.BORDER_COLOR, SeparatorOrientation.HORIZONTAL);
        optionPanel.add(separator, gbc);
        return optionPanel;
    }

    private @NotNull JPanel customAnnoPanel() {
        String customAnno = httpSetting.getCustomAnno();

        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.add(new JLabel(Bundle.get("http.extension.setting.option.base.custom.controller.annotation") + " "), BorderLayout.WEST);

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
                    project.getMessageBus().syncPublisher(TreeTopic.REFRESH_TOPIC).refresh(false);
                }
            }
        });

        panel.add(annoTextField, BorderLayout.CENTER);

        return panel;
    }
}