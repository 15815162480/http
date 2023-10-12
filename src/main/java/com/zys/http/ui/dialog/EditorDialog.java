package com.zys.http.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.UIConstant;
import com.zys.http.service.Bundle;
import com.zys.http.ui.editor.CustomEditor;
import jdk.jfr.Description;
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
@Description("编辑器对话框")
public class EditorDialog extends DialogWrapper {

    private final CustomEditor customEditor;

    @Setter
    @Nullable
    private Consumer<String> okCallBack;

    public EditorDialog(Project project, String title, CustomEditor customEditor) {
        super(project);
        this.customEditor = customEditor;
        customEditor.setBorder(JBUI.Borders.customLine(UIConstant.EDITOR_BORDER_COLOR, 1, 1, 1, 1));
        init();
        getRootPane().setMinimumSize(new Dimension(800, 600));
        getRootPane().setMaximumSize(new Dimension(800, 600));
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
