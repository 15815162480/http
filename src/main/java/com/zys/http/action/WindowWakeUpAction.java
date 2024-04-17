package com.zys.http.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.zys.http.constant.HttpConstant;
import com.zys.http.extension.service.Bundle;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author zhou ys
 * @since 2024-04-17
 */
public class WindowWakeUpAction extends AnAction {
    public WindowWakeUpAction() {
        this.getTemplatePresentation().setText(Bundle.get("http.window.short.cut.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = Objects.requireNonNull(event.getProject());
        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = manager.getToolWindow(HttpConstant.PLUGIN_NAME);
        if (Objects.nonNull(toolWindow)) {
            toolWindow.show();
        }
    }
}
