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
import com.zys.http.action.FilterAction;
import com.zys.http.action.RefreshAction;
import com.zys.http.action.group.EnvActionGroup;
import com.zys.http.constant.HttpEnum;
import com.zys.http.service.Bundle;
import com.zys.http.tool.FreeMakerTool;
import com.zys.http.ui.popup.MethodFilterPopup;
import com.zys.http.ui.tree.HttpApiTreePanel;
import com.zys.http.ui.window.panel.RequestPanel;
import jdk.jfr.Description;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * @author zys
 * @since 2023-08-13
 */
@Description("请求标签页面")
public class RequestTabWindow extends SimpleToolWindowPanel implements Disposable {
    private final RequestPanel requestPanel;

    @Description("请求方式过滤菜单")
    private final MethodFilterPopup methodFilterPopup;

    private final transient ExecutorService executorTaskBounded = new ThreadPoolExecutor(
            1,
            1,
            5L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );

    public RequestTabWindow(RequestPanel requestPanel) {
        super(true, true);
        this.requestPanel = requestPanel;
        this.methodFilterPopup = new MethodFilterPopup(
                Arrays.stream(HttpEnum.HttpMethod.values()).filter(o -> !o.equals(HttpEnum.HttpMethod.REQUEST))
                        .toList()
        );
        methodFilterPopup.setChangeAllCb((list, b) -> refreshTree(true));
        methodFilterPopup.setChangeCb((method, b) -> refreshTree(true));
        init();
    }

    @Description("初始化")
    private void init() {
        setToolbar(requestToolBar().getComponent());
        setContent(requestPanel);
        refreshTree(false);
    }

    @Description("初始化顶部工具栏")
    private ActionToolbar requestToolBar() {
        DefaultActionGroup group = new DefaultActionGroup();
        EnvActionGroup envActionGroup = new EnvActionGroup(requestPanel);
        group.add(envActionGroup);
        RefreshAction refreshAction = new RefreshAction();
        refreshAction.setAction(event -> {
            requestPanel.reload(null);
            requestPanel.getHttpApiTreePanel().clear();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    refreshTree(false);
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

        group.addSeparator();
        FilterAction filterAction = new FilterAction(Bundle.get("http.filter.action"));
        // filterAction.setAction(e -> methodFilterPopup.show(requestPanel, methodFilterPopup.getX(), methodFilterPopup.getY()));
        filterAction.setAction(e -> {
            try {
                FreeMakerTool.exportEnv(requestPanel.getServiceTool().getSelectedEnv(), requestPanel.getServiceTool().getDefaultHttpConfig());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        group.add(filterAction);

        ActionToolbar topToolBar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true);
        topToolBar.setTargetComponent(this);
        return topToolBar;
    }

    private void refreshTree(boolean isExpand) {
        Project project = requestPanel.getProject();
        DumbService.getInstance(project).smartInvokeLater(
                () -> {
                    HttpApiTreePanel httpApiTreePanel = requestPanel.getHttpApiTreePanel();
                    List<HttpEnum.HttpMethod> selectedValues = methodFilterPopup.getSelectedValues();
                    ReadAction.nonBlocking(() -> httpApiTreePanel.initNodes(selectedValues))
                            .inSmartMode(project)
                            .finishOnUiThread(ModalityState.defaultModalityState(), root -> {
                                httpApiTreePanel.render(root);
                                if (isExpand) {
                                    requestPanel.getHttpApiTreePanel().expandAll();
                                }
                            })
                            .submit(executorTaskBounded);
                }
        );
    }

    @Override
    public void dispose() {
        executorTaskBounded.shutdown();
    }
}
