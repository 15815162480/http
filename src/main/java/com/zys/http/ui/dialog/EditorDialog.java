package com.zys.http.ui.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.UIConstant;
import com.zys.http.service.Bundle;
import com.zys.http.tool.ProjectTool;
import com.zys.http.ui.editor.CustomEditor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author zys
 * @since 2023-09-19
 */

@Accessors(chain = true)
public class EditorDialog extends DialogWrapper {

    private final CustomEditor customEditor;

    @Setter
    private Consumer<String> okCallBack;

    public EditorDialog(String title, CustomEditor customEditor) {
        super(ProjectTool.getProject());
        this.customEditor = customEditor;
        customEditor.setBorder(JBUI.Borders.customLine(UIConstant.EDITOR_BORDER_COLOR, 1, 1, 1, 1));
        init();
        getRootPane().setMinimumSize(new Dimension(800, 600));
        setOKButtonText(Bundle.get("http.text.ok"));
        setAutoAdjustable(true);
        setTitle(title);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return customEditor;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction()};
    }

    @Override
    protected void doOKAction() {
        if (Objects.nonNull(okCallBack)) {
            okCallBack.accept(customEditor.getText());
        }
        super.doOKAction();
    }
}
