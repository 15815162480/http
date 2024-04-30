package com.zys.http.extension.setting;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.NumberDocument;
import com.intellij.ui.SeparatorFactory;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.FormBuilder;
import com.zys.http.extension.service.Bundle;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

/**
 * @author zys
 * @since 2024-04-13
 */
public class HttpSettingPanel extends JBPanel<HttpSettingPanel> {
    @Getter
    private final transient HttpSetting.State state = new HttpSetting.State();
    private final JBCheckBox defaultBox = new JBCheckBox(Bundle.get("http.extension.setting.option.base.default.env"));
    private final JBCheckBox vcsBox = new JBCheckBox(Bundle.get("http.extension.setting.option.base.vcs.change"));
    private final JBCheckBox seBox = new JBCheckBox(Bundle.get("http.extension.setting.option.base.search.everywhere"));
    private final JTextField annoTextField = new JTextField(30);
    private final JTextField timeTf = new JTextField(20);

    public HttpSettingPanel() {
        super(new BorderLayout(0, 0));
        init();
        copyState();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                transferFocus();
            }
        });
    }

    public void init() {
        FormBuilder formBuilder = FormBuilder.createFormBuilder();
        formBuilder.addComponent(createBasicSettingPanel());
        formBuilder.addComponent(createRequestSettingPanel());
        add(formBuilder.getPanel(), BorderLayout.NORTH);
    }

    private @NotNull JPanel createBasicSettingPanel() {
        FormBuilder main = FormBuilder.createFormBuilder();

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.add(new JLabel("     "), BorderLayout.WEST);
        TitledSeparator separator = SeparatorFactory.createSeparator(Bundle.get("http.extension.setting.option.base"), content);
        main.addComponent(separator);

        FormBuilder formBuilder = new FormBuilder();
        defaultBox.addChangeListener(l -> state.setGenerateDefault(defaultBox.isSelected()));
        formBuilder.addComponent(defaultBox);
        vcsBox.addChangeListener(l -> state.setRefreshWhenVcsChange(vcsBox.isSelected()));
        formBuilder.addComponent(vcsBox);
        seBox.addChangeListener(l -> state.setEnableSearchEverywhere(seBox.isSelected()));
        formBuilder.addComponent(seBox);

        String customAnno = state.getCustomAnno();
        JPanel annoPanel = new JPanel(new BorderLayout(0, 0));
        annoPanel.add(new JLabel(Bundle.get("http.extension.setting.option.base.custom.controller.annotation") + " "), BorderLayout.WEST);

        if (CharSequenceUtil.isNotBlank(customAnno)) {
            annoTextField.setText(customAnno.trim());
        }
        annoTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String text = annoTextField.getText().trim();
                if (CharSequenceUtil.isBlank(text)) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        annoTextField.setText("");
                        state.setCustomAnno("");
                    });
                    return;
                }
                state.setCustomAnno(text);
            }
        });

        annoPanel.add(annoTextField, BorderLayout.CENTER);
        formBuilder.addComponent(annoPanel);

        content.add(formBuilder.getPanel(), BorderLayout.CENTER);
        main.addComponent(content);
        return main.getPanel();
    }

    private @NotNull JPanel createRequestSettingPanel() {
        FormBuilder formBuilder = FormBuilder.createFormBuilder();

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.add(new JLabel("     "), BorderLayout.WEST);
        TitledSeparator separator = SeparatorFactory.createSeparator(Bundle.get("http.extension.setting.option.request"), content);
        formBuilder.addComponent(separator);

        JPanel requestTimeoutPanel = new JPanel(new BorderLayout(0, 0));
        requestTimeoutPanel.add(new JLabel(Bundle.get("http.extension.setting.option.request.timeout") + " "), BorderLayout.WEST);
        timeTf.setDocument(new NumberDocument());
        timeTf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String text = timeTf.getText();
                if (CharSequenceUtil.isBlank(text)) {
                    ApplicationManager.getApplication().invokeLater(() -> state.setTimeout(HttpSetting.DEFAULT_TIMEOUT));
                    return;
                }
                state.setTimeout(Integer.parseInt(text));
            }
        });
        requestTimeoutPanel.add(timeTf, BorderLayout.CENTER);
        content.add(requestTimeoutPanel, BorderLayout.CENTER);
        formBuilder.addComponent(content);
        return formBuilder.getPanel();
    }

    public void reset() {
        HttpSetting.State oldState = HttpSetting.getInstance().getState();
        if (Objects.isNull(oldState)) {
            return;
        }
        copyState();
    }

    private void copyState() {
        HttpSetting httpSetting = HttpSetting.getInstance();
        state.setCustomAnno(httpSetting.getCustomAnno());
        state.setEnableSearchEverywhere(httpSetting.getEnableSearchEverywhere());
        state.setTimeout(httpSetting.getTimeout());
        state.setGenerateDefault(httpSetting.getGenerateDefault());
        state.setRefreshWhenVcsChange(httpSetting.getRefreshWhenVcsChange());

        defaultBox.setSelected(httpSetting.getGenerateDefault());
        vcsBox.setSelected(httpSetting.getRefreshWhenVcsChange());
        seBox.setSelected(httpSetting.getEnableSearchEverywhere());
        annoTextField.setText(httpSetting.getCustomAnno());
        timeTf.setText(httpSetting.getTimeout() + "");
    }
}