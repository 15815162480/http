package com.zys.http.ui.popup;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.UIConstant;
import com.zys.http.service.Bundle;
import jdk.jfr.Description;
import lombok.Getter;
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
@Description("请求方法过滤菜单")
public class MethodFilterPopup extends JPopupMenu {

    @Getter
    private transient HttpEnum.HttpMethod[] values;

    private transient HttpEnum.HttpMethod[] defaultValues;

    @Getter
    private final List<JBCheckBox> checkBoxList = new ArrayList<>();

    @Setter
    @Nullable
    @Description("选中的单个数据, 对应的选择框")
    private transient BiConsumer<HttpEnum.HttpMethod, JCheckBox> changeCb;

    @Setter
    @Nullable
    @Description("选中的所有数据, 是否选中")
    private transient BiConsumer<List<HttpEnum.HttpMethod>, Boolean> changeAllCb;

    public MethodFilterPopup(HttpEnum.HttpMethod[] values) {
        this.values = values;
        this.defaultValues = values;
        render(values, defaultValues);
    }

    public void render(@NotNull HttpEnum.HttpMethod[] values, HttpEnum.HttpMethod[] defaultValues) {
        this.values = values;
        this.defaultValues = defaultValues;
        init();
    }

    public void init() {
        JPanel checkboxPane = new JPanel();
        JPanel buttonPane = new JPanel();
        this.setLayout(new BorderLayout(0, 0));
        checkboxPane.setLayout(new GridLayout(values.length, 1, 3, 3));
        for (HttpEnum.HttpMethod method : values) {
            JBCheckBox checkBox = new JBCheckBox(method.name(), selected(method));
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
            checkBoxList.forEach(v -> v.setSelected(true));
            if (changeAllCb != null) {
                changeAllCb.accept(List.of(values), true);
            }
        });
        buttonPane.add(selectAll);

        JButton unSelectAll = new JButton(Bundle.get("http.filter.action.method.unselect.all"));
        unSelectAll.addActionListener(e -> {
            checkBoxList.forEach(v -> v.setSelected(false));
            if (changeAllCb != null) {
                changeAllCb.accept(List.of(values), false);
            }
        });
        buttonPane.add(unSelectAll);

        JButton close = new JButton(Bundle.get("http.filter.action.method.close"));
        close.addActionListener(e -> this.setVisible(false));
        buttonPane.add(close);
        buttonPane.setBorder(JBUI.Borders.customLineTop(UIConstant.BORDER_COLOR));

        this.add(checkboxPane, BorderLayout.CENTER);
        this.add(buttonPane, BorderLayout.SOUTH);
    }

    private boolean selected(HttpEnum.HttpMethod t) {
        for (HttpEnum.HttpMethod defaultValue : defaultValues) {
            if (defaultValue.equals(t)) {
                return true;
            }
        }
        return false;
    }


    public HttpEnum.HttpMethod[] setDefaultValues(HttpEnum.HttpMethod[] defaultValues) {
        this.defaultValues = defaultValues;
        return this.defaultValues;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        setDefaultValues(getSelectedValues());
    }




    public HttpEnum.HttpMethod[] getSelectedValues() {
        int size = getCheckBoxList().size();
        List<HttpEnum.HttpMethod> methods = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            if (getCheckBoxList().get(i).isSelected()) {
                methods.add(getValues()[i]);
            }
        }
        return setDefaultValues(methods.toArray(new HttpEnum.HttpMethod[]{}));
    }
}
