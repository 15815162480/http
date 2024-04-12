package com.zys.http.extension.setting;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.HttpConstant;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.topic.RefreshTreeTopic;
import com.zys.http.tool.ui.DialogTool;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author zhou ys
 * @since 2024-04-12
 */
public class HttpSettingConfigurable implements Configurable {
    private final HttpSettingPanel httpSettingPanel = new HttpSettingPanel();

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return HttpConstant.PLUGIN_NAME;
    }

    @Override
    public @Nullable JComponent createComponent() {
        return httpSettingPanel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        HttpSetting.getInstance().setGenerateDefault(httpSettingPanel.getDefaultBox().isSelected());
        HttpSetting.getInstance().setEnableSearchEverywhere(httpSettingPanel.getSeBox().isSelected());
        HttpSetting.getInstance().setRefreshWhenVcsChange(httpSettingPanel.getVcsBox().isSelected());
        HttpSetting.getInstance().setCustomAnno(httpSettingPanel.getAnnoTextField().getText());
    }

    @Getter
    public static class HttpSettingPanel extends JBPanel<HttpSettingPanel> {
        private final HttpSetting httpSetting;
        private final JBCheckBox defaultBox = new JBCheckBox(Bundle.get("http.api.icon.setting.action.default.env"));
        private final JBCheckBox vcsBox = new JBCheckBox(Bundle.get("http.api.icon.setting.action.vcs.change"));
        private final JBCheckBox seBox = new JBCheckBox(Bundle.get("http.api.icon.setting.action.search.everywhere"));
        private final JTextField annoTextField = new JTextField(30);

        public HttpSettingPanel() {
            super(new BorderLayout(0, 0));
            httpSetting = HttpSetting.getInstance();
            init();
        }

        public void init() {
            initGenerateDefaultBox();
            initRefreshWhenVcsChangeBox();
            initEnableSearchEverywhereBox();
            JPanel main = new JPanel(new BorderLayout(0, 0));

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = JBUI.insets(5, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridy = 0;
            panel.add(defaultBox, gbc);

            gbc.gridy = 1;
            panel.add(vcsBox, gbc);

            gbc.gridy = 2;
            panel.add(seBox, gbc);

            gbc.gridy = 3;
            panel.add(customAnnoPanel(), gbc);

            main.add(panel, BorderLayout.WEST);
            add(main, BorderLayout.NORTH);
        }


        private void initGenerateDefaultBox() {
            defaultBox.setSelected(httpSetting.getGenerateDefault());
            defaultBox.addItemListener(e -> ApplicationManager.getApplication().invokeLater(() -> {
                @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
                for (Project project : openProjects) {
                    project.getMessageBus().syncPublisher(RefreshTreeTopic.TOPIC).refresh(false);
                }
            }));
        }


        private void initRefreshWhenVcsChangeBox() {
            vcsBox.setSelected(httpSetting.getRefreshWhenVcsChange());
        }

        private void initEnableSearchEverywhereBox() {

            seBox.setSelected(httpSetting.getEnableSearchEverywhere());

        }

        private @NotNull JPanel customAnnoPanel() {
            String customAnno = httpSetting.getCustomAnno();

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
                            httpSetting.setCustomAnno("");
                            annoTextField.setText("");
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
            return panel;
        }
    }
}
