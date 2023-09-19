package com.zys.http.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.zys.http.service.Bundle;
import com.zys.http.ui.editor.CustomEditor;
import lombok.Getter;
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
@Getter
@Setter
@Accessors(chain = true)
public class EditorDialog extends DialogWrapper {

    private final CustomEditor customEditor;

    private Consumer<String> okCallBack;

    public EditorDialog(@Nullable Project project, String title, CustomEditor customEditor) {
        super(project);
        this.customEditor = customEditor;
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
        if (Objects.nonNull(okCallBack)){
            okCallBack.accept(customEditor.getText());
        }
        super.doOKAction();
    }
}
