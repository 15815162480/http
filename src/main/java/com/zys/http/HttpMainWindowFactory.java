package com.zys.http;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.ui.window.RequestTabWindow;
import com.zys.http.ui.window.panel.RequestPanel;
import org.jetbrains.annotations.NotNull;

/**
 * @author zys
 * @since 2023-08-13
 */
public class HttpMainWindowFactory implements ToolWindowFactory, DumbAware {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        initTool(project);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(new RequestTabWindow(new RequestPanel()), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public void initTool(@NotNull Project project){
        HttpServiceTool.initHttpService(project);
    }
}
