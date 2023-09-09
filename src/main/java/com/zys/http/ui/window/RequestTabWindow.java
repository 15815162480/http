package com.zys.http.ui.window;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.zys.http.action.EnvAction;
import com.zys.http.action.TestAction;
import com.zys.http.ui.dialog.EnvShowDialog;
import com.zys.http.ui.icon.HttpIcons;
import com.zys.http.ui.window.panel.RequestPanel;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-08-13
 */
@Description("请求标签页面")
public class RequestTabWindow extends SimpleToolWindowPanel {
    private final transient Project project;

    public RequestTabWindow(Project project) {
        super(true, true);
        this.project = project;
        init();
    }

    @Description("初始化")
    private void init() {
        requestToolBar();
        requestPanel();
    }

    @Description("初始化顶部工具栏")
    private void requestToolBar() {
        DefaultActionGroup http = new DefaultActionGroup();
        EnvAction envAction = new EnvAction();
        envAction.setAction(e -> new EnvShowDialog(project).show());
        http.add(envAction);
        http.add(new TestAction(HttpIcons.REQUEST));
        http.add(new TestAction(HttpIcons.GET));
        http.add(new TestAction(HttpIcons.POST));
        http.add(new TestAction(HttpIcons.PUT));
        http.add(new TestAction(HttpIcons.DELETE));
        http.add(new TestAction(HttpIcons.PATCH));
        http.add(new TestAction(HttpIcons.HEADER));
        http.add(new TestAction(HttpIcons.OPTIONS));
        ActionToolbar topToolBar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, http, true);
        topToolBar.setTargetComponent(this);
        setToolbar(topToolBar.getComponent());
    }

    @Description("初始化顶部工具栏")
    private void requestPanel() {
        setContent(new RequestPanel(project));
    }
}
