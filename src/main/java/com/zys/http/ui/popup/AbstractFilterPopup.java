package com.zys.http.ui.popup;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.UIConstant;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.topic.TreeTopic;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhou ys
 * @since 2023-09-21
 */
public abstract class AbstractFilterPopup<T> extends JPopupMenu {

    protected final transient List<JBCheckBox> checkBoxList = new ArrayList<>();

    protected final transient List<T> values;
    protected transient List<T> defaultValues;

    protected final transient Project project;

    protected AbstractFilterPopup(Project project, List<T> values) {
        this.project = project;
        this.values = values;
        this.defaultValues = values;
        init();
    }

    public void init() {
        JPanel checkboxPane = new JPanel();
        JPanel buttonPane = new JPanel();
        this.setLayout(new BorderLayout(0, 0));
        checkboxPane.setLayout(new GridLayout(values.size(), 1, 3, 3));
        for (T value : values) {
            JBCheckBox checkBox = new JBCheckBox(value.toString(), selected(value));
            checkBox.addActionListener(e -> project.getMessageBus().syncPublisher(TreeTopic.REFRESH_TOPIC).refresh(true));
            checkBoxList.add(checkBox);
            checkboxPane.add(checkBox);
        }
        JButton selectAll = new JButton(Bundle.get("http.api.icon.node.filter.select.all"));
        selectAll.addActionListener(e -> {
            checkBoxList.forEach(v -> v.setSelected(true));
            project.getMessageBus().syncPublisher(TreeTopic.REFRESH_TOPIC).refresh(true);
        });
        buttonPane.add(selectAll);

        JButton unSelectAll = new JButton(Bundle.get("http.api.icon.node.filter.unselect.all"));
        unSelectAll.addActionListener(e -> {
            checkBoxList.forEach(v -> v.setSelected(false));
            project.getMessageBus().syncPublisher(TreeTopic.REFRESH_TOPIC).refresh(true);
        });
        buttonPane.add(unSelectAll);

        JButton close = new JButton(Bundle.get("http.api.icon.node.filter.close"));
        close.addActionListener(e -> this.setVisible(false));
        buttonPane.add(close);
        buttonPane.setBorder(JBUI.Borders.customLineTop(UIConstant.BORDER_COLOR));

        this.add(checkboxPane, BorderLayout.CENTER);
        this.add(buttonPane, BorderLayout.SOUTH);
    }

    private boolean selected(T t) {
        for (T defaultValue : defaultValues) {
            if (defaultValue.equals(t)) {
                return true;
            }
        }
        return false;
    }

    public List<T> setDefaultValues(List<T> defaultValues) {
        this.defaultValues = defaultValues;
        return this.defaultValues;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        setDefaultValues(getSelectedValues());
    }

    public List<T> getSelectedValues() {
        int size = checkBoxList.size();
        List<T> selectValues = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            if (checkBoxList.get(i).isSelected()) {
                selectValues.add(this.values.get(i));
            }
        }
        return setDefaultValues(selectValues);
    }
}
