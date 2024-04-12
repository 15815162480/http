package com.zys.http;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.zys.http.constant.HttpConstant;
import com.zys.http.extension.service.Bundle;
import com.zys.http.ui.window.EnvironmentTabWindow;
import com.zys.http.ui.window.HistoryTabWindow;
import com.zys.http.ui.window.RequestTabWindow;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author zys
 * @since 2023-08-13
 */
@Description("创建 ToolWindow 工厂")
public class HttpMainWindowFactory implements ToolWindowFactory, DumbAware {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.getInstance();

        RequestTabWindow requestTabWindow = new RequestTabWindow(project);
        Content apiContent = contentFactory.createContent(requestTabWindow, Bundle.get("http.window.tab.api"), false);
        toolWindow.getContentManager().addContent(apiContent);

        EnvironmentTabWindow environmentTabWindow = new EnvironmentTabWindow(project);
        Content envContent = contentFactory.createContent(environmentTabWindow, Bundle.get("http.window.tab.env"), false);
        toolWindow.getContentManager().addContent(envContent);

        HistoryTabWindow historyTabWindow = new HistoryTabWindow(project);
        Content hisContent = contentFactory.createContent(historyTabWindow, Bundle.get("http.window.tab.history"), false);
        toolWindow.getContentManager().addContent(hisContent);

        // SettingTabWindow settingTabWindow = new SettingTabWindow(project);
        // Content settingContent = contentFactory.createContent(settingTabWindow, Bundle.get("http.window.tab.setting"), false);
        // toolWindow.getContentManager().addContent(settingContent);
    }

    public static class WindowWakeUpAction extends AnAction {
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
}
