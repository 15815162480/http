package com.zys.http;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.zys.http.extension.service.Bundle;
import com.zys.http.ui.window.EnvironmentTabWindow;
import com.zys.http.ui.window.RequestTabWindow;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

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
        EnvironmentTabWindow environmentTabWindow = new EnvironmentTabWindow(project);
        requestTabWindow.setGenerateDefaultCb(environmentTabWindow::reloadEnv);

        Content apiContent = contentFactory.createContent(requestTabWindow, Bundle.get("http.window.api"), false);
        toolWindow.getContentManager().addContent(apiContent);

        Content envContent = contentFactory.createContent(environmentTabWindow, Bundle.get("http.window.env"), false);
        toolWindow.getContentManager().addContent(envContent);
    }
}
