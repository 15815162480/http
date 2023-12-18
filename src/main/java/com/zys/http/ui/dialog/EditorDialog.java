package com.zys.http.ui.dialog;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.UIConstant;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.topic.EditorDialogOkTopic;
import com.zys.http.ui.editor.CustomEditor;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author zys
 * @since 2023-09-19
 */
@Description("编辑器对话框")
public class EditorDialog extends DialogWrapper {
    private final String title;
    private final Project project;
    private final CustomEditor customEditor;

    public EditorDialog(Project project, String title, FileType fileType, String editorText) {
        super(project);
        this.title = title;
        this.project = project;
        this.customEditor = new CustomEditor(project, fileType);
        this.customEditor.setText(editorText);
        this.customEditor.setBorder(JBUI.Borders.customLine(UIConstant.EDITOR_BORDER_COLOR, 1));
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
        // 如果是请求体标签页, 修改后需要回显
        if (title.equals(Bundle.get("http.editor.body.action.dialog"))) {
            project.getMessageBus().syncPublisher(EditorDialogOkTopic.TOPIC).modify(customEditor.getText(), true);
        }

        // 如果是请求头编辑
        if (title.equals(Bundle.get("http.editor.header.properties.dialog"))) {
            project.getMessageBus().syncPublisher(EditorDialogOkTopic.TOPIC).properties(customEditor.getText(), true);
        }

        // 如果是请求参数编辑
        if (title.equals(Bundle.get("http.editor.param.properties.dialog"))) {
            project.getMessageBus().syncPublisher(EditorDialogOkTopic.TOPIC).properties(customEditor.getText(), false);
        }
        super.doOKAction();
    }
}
