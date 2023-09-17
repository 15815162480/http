package com.zys.http.ui.window;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.zys.http.action.CollapseAction;
import com.zys.http.action.ExpandAction;
import com.zys.http.action.RefreshAction;
import com.zys.http.action.group.EnvActionGroup;
import com.zys.http.constant.HttpEnum;
import com.zys.http.entity.HttpConfig;
import com.zys.http.ui.tree.HttpApiTreePanel;
import com.zys.http.ui.window.panel.RequestPanel;
import jdk.jfr.Description;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * @author zys
 * @since 2023-08-13
 */
@Description("请求标签页面")
public class RequestTabWindow extends SimpleToolWindowPanel implements Disposable {
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
    @SuppressWarnings("ExtractMethodRecommender")
    private void requestToolBar() {
        DefaultActionGroup group = new DefaultActionGroup();
        EnvActionGroup envActionGroup = new EnvActionGroup(requestPanel);
        group.add(envActionGroup);
        RefreshAction refreshAction = new RefreshAction();
        refreshAction.setAction(event -> {
            requestPanel.getHttpApiTreePanel().clear();
            requestPanel.getHostTextField().setText("");
            requestPanel.getHttpMethodComboBox().setSelectedItem(HttpEnum.HttpMethod.GET);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    refreshTree();
                }
            }, 500);
        });
        group.add(refreshAction);
        group.addSeparator();

        ExpandAction expandAction = new ExpandAction();
        expandAction.setAction(event -> requestPanel.getHttpApiTreePanel().treeExpand());
        group.add(expandAction);
        CollapseAction collapseAction = new CollapseAction();
        collapseAction.setAction(event -> requestPanel.getHttpApiTreePanel().treeCollapse());
        group.add(collapseAction);


        ActionToolbar topToolBar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true);
        topToolBar.setTargetComponent(this);
        setToolbar(topToolBar.getComponent());
    }

    @Description("初始化顶部内容")
    private void requestPanel() {
        setContent(requestPanel);
        refreshTree();
    }

    private void refreshTree() {
        DumbService.getInstance(project).smartInvokeLater(
                () -> {
                    HttpApiTreePanel httpApiTreePanel = requestPanel.getHttpApiTreePanel();
                    ReadAction.nonBlocking(httpApiTreePanel::initNodes)
                            .inSmartMode(project)
                            .finishOnUiThread(ModalityState.defaultModalityState(), httpApiTreePanel::render)
                            .submit(executorTaskBounded);
                    // 设置默认环境
                    HttpConfig config = new HttpConfig();
                    config.setProtocol(HttpEnum.Protocol.HTTP);

                }
        );
    }

    @Override
    public void dispose() {
        executorTaskBounded.shutdown();
    }
}
