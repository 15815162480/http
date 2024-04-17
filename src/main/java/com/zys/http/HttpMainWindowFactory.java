package com.zys.http;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.zys.http.extension.service.Bundle;
import com.zys.http.window.environment.EnvironmentWindow;
import com.zys.http.window.history.HistoryWindow;
import com.zys.http.window.request.RequestWindow;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

/**
 * @author zys
 * @since 2023-08-13
 */
@Description("创建 ToolWindow 工厂")
public class HttpMainWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.getInstance();

        RequestWindow requestWindow = new RequestWindow(project);
        Content apiContent = contentFactory.createContent(requestWindow, Bundle.get("http.window.tab.api"), false);
        toolWindow.getContentManager().addContent(apiContent);

        EnvironmentWindow environmentWindow = new EnvironmentWindow(project);
        Content envContent = contentFactory.createContent(environmentWindow, Bundle.get("http.window.tab.env"), false);
        toolWindow.getContentManager().addContent(envContent);

        HistoryWindow historyWindow = new HistoryWindow(project);
        Content hisContent = contentFactory.createContent(historyWindow, Bundle.get("http.window.tab.history"), false);
        toolWindow.getContentManager().addContent(hisContent);
    }
}
