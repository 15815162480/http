package com.zys.http.ui.window;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.zys.http.action.EnvAction;
import com.zys.http.action.TestAction;
import com.zys.http.ui.dialog.EnvShowDialog;
import com.zys.http.ui.icon.HttpIcons;
import com.zys.http.ui.tree.HttpApiTreePanel;
import com.zys.http.ui.window.panel.RequestPanel;
import jdk.jfr.Description;

import java.util.concurrent.*;

/**
 * @author zys
 * @since 2023-08-13
 */
@Description("请求标签页面")
public class RequestTabWindow extends SimpleToolWindowPanel {
    private final transient Project project;

    private final RequestPanel requestPanel;
    private final transient ExecutorService executorTaskBounded = new ThreadPoolExecutor(
            1,
            1,
            5L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );

    public RequestTabWindow(Project project, RequestPanel requestPanel) {
        super(true, true);
        this.project = project;
        this.requestPanel = requestPanel;
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

    @Description("初始化顶部内容")
    private void requestPanel() {
        setContent(requestPanel);
        DumbService.getInstance(project).smartInvokeLater(
                () -> {
                    HttpApiTreePanel httpApiTreePanel = requestPanel.getTopPart().getHttpApiTreePanel();
                    ReadAction.nonBlocking(httpApiTreePanel::initNodes)
                            .finishOnUiThread(ModalityState.defaultModalityState(), httpApiTreePanel::render)
                            .submit(executorTaskBounded);
                }
        );
    }
}
