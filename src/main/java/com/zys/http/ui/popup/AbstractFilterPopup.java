package com.zys.http.ui.popup;

import com.intellij.ui.components.JBCheckBox;
import com.zys.http.service.Bundle;
import jdk.jfr.Description;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * @author zhou ys
 * @since 2023-09-20
 */
@Description("弹出菜单")
public abstract class AbstractFilterPopup<T> extends JPopupMenu {
    private transient T[] values;

    private transient T[] defaultValues;
    private final List<JBCheckBox> checkBoxList = new ArrayList<>();

    @Setter
    @Nullable
    @Description("选中的单个数据, 对应的选择框")
    private transient BiConsumer<T, JCheckBox> changeCb;

    @Setter
    @Nullable
    @Description("选中的所有数据, 是否选中")
    private transient BiConsumer<List<T>, Boolean> changeAllCb;

    protected AbstractFilterPopup(T[] values) {
        this(values, values);
    }

    protected AbstractFilterPopup(@NotNull T[] values, T[] defaultValues) {
        super();
        render(values, defaultValues);
    }

    public void render(@NotNull T[] values, T[] defaultValues) {
        this.values = values;
        this.defaultValues = defaultValues;
        init();
    }

    public void init() {
        JPanel checkboxPane = new JPanel();
        JPanel buttonPane = new JPanel();
        this.setLayout(new BorderLayout(0, 0));
        checkboxPane.setLayout(new GridLayout(values.length, 1, 3, 3));
        for (T method : values) {
            JBCheckBox checkBox = new JBCheckBox(method.toString(), selected(method));
            checkBox.addActionListener(e -> {
                if (Objects.nonNull(changeCb)) {
                    changeCb.accept(method, checkBox);
                }
            });
            checkBoxList.add(checkBox);
            checkboxPane.add(checkBox);
        }
        JButton selectAll = new JButton(Bundle.get("http.filter.action.method.select.all"));
        selectAll.addActionListener(e -> {
            if (getSelectedValues().length >= checkBoxList.size()) {
                return;
            }
            List<T> changes = new ArrayList<>();
            // 检查其他的是否被选中, 如果没有就选中他们
            for (int i = 0; i < checkBoxList.size(); i++) {
                JCheckBox checkBox = checkBoxList.get(i);
                if (!checkBox.isSelected()) {
                    checkBox.setSelected(true);
                    changes.add(this.values[i]);
                }
            }
            if (!changes.isEmpty() && changeAllCb != null) {
                changeAllCb.accept(changes, true);
            }
        });
        buttonPane.add(selectAll);

        JButton unSelectAll = new JButton(Bundle.get("http.filter.action.method.unselect.all"));
        unSelectAll.addActionListener(e -> {
            List<T> changes = new ArrayList<>();
            for (int i = 0; i < checkBoxList.size(); i++) {
                JCheckBox checkBox = checkBoxList.get(i);
                if (checkBox.isSelected()) {
                    checkBox.setSelected(false);
                    changes.add(this.values[i]);
                }
            }
            if (!changes.isEmpty() && changeAllCb != null) {
                changeAllCb.accept(changes, false);
            }
        });
        buttonPane.add(unSelectAll);

        JButton close = new JButton(Bundle.get("http.filter.action.method.close"));
        close.addActionListener(e -> this.setVisible(false));
        buttonPane.add(close);

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

    @SuppressWarnings("unchecked")
    public T[] getSelectedValues() {
        List<T> selectedValues = new ArrayList<>();

        for (int i = 0; i < checkBoxList.size(); i++) {
            if (checkBoxList.get(i).isSelected()) {
                selectedValues.add(this.values[i]);
            }
        }
        return setDefaultValues((T[]) selectedValues.toArray());
    }

    public T[] setDefaultValues(T[] defaultValues) {
        this.defaultValues = defaultValues;
        return this.defaultValues;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        setDefaultValues(getSelectedValues());
    }
}
