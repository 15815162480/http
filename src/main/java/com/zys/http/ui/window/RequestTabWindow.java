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
import com.zys.http.action.*;
import com.zys.http.action.group.EnvActionGroup;
import com.zys.http.action.group.SelectActionGroup;
import com.zys.http.constant.HttpEnum;
import com.zys.http.service.Bundle;
import com.zys.http.ui.dialog.EnvAddOrEditDialog;
import com.zys.http.ui.dialog.EnvListShowDialog;
import com.zys.http.ui.icon.HttpIcons;
import com.zys.http.ui.popup.MethodFilterPopup;
import com.zys.http.ui.popup.NodeShowFilterPopup;
import com.zys.http.ui.tree.HttpApiTreePanel;
import com.zys.http.ui.window.panel.RequestPanel;
import jdk.jfr.Description;

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

    private final transient Project project;

    private final RequestPanel requestPanel;

    @Description("请求方式过滤菜单")
    private final MethodFilterPopup methodFilterPopup;
    @Description("结点展示过滤")
    private final NodeShowFilterPopup nodeShowFilterPopup;

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
        this.project = requestPanel.getProject();
        this.methodFilterPopup = new MethodFilterPopup(
                Arrays.stream(HttpEnum.HttpMethod.values()).filter(o -> !o.equals(HttpEnum.HttpMethod.REQUEST))
                        .toList()
        );
        this.nodeShowFilterPopup = new NodeShowFilterPopup();
        methodFilterPopup.setChangeAllCb((list, b) -> refreshTree(true));
        methodFilterPopup.setChangeCb((method, b) -> refreshTree(true));

        nodeShowFilterPopup.setChangeAllCb((list, b) -> refreshTree(true));
        nodeShowFilterPopup.setChangeCb((method, b) -> refreshTree(true));
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
        EnvActionGroup envActionGroup = new EnvActionGroup();

        AddAction addAction = new AddAction(Bundle.get("http.action.add.env"));
        addAction.setAction(event -> new EnvAddOrEditDialog(project, true, "").show());
        envActionGroup.add(addAction);

        SelectActionGroup selectActionGroup = new SelectActionGroup();
        selectActionGroup.setPopup(true);
        selectActionGroup.setCallback(s -> requestPanel.reload(requestPanel.getHttpApiTreePanel().getChooseNode()));
        envActionGroup.add(selectActionGroup);

        CommonAction envListAction = new CommonAction(Bundle.get("http.action.show.env"), "Env list", null);
        envListAction.setAction(event -> {
            EnvListShowDialog dialog = new EnvListShowDialog(requestPanel.getProject());
            dialog.getEnvShowTable().setEditOKCb(n -> requestPanel.reload(requestPanel.getHttpApiTreePanel().getChooseNode()));
            dialog.show();
        });
        envActionGroup.add(envListAction);
        envActionGroup.add(createExportActionGroup());

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
        CommonAction settingAction = new CommonAction(Bundle.get("http.filter.action.node.show"), "Setting", HttpIcons.General.SETTING);
        settingAction.setAction(e -> nodeShowFilterPopup.show(requestPanel, nodeShowFilterPopup.getX(), nodeShowFilterPopup.getY()));
        group.add(settingAction);

        FilterAction filterAction = new FilterAction(Bundle.get("http.filter.action"));
        filterAction.setAction(e -> methodFilterPopup.show(requestPanel, methodFilterPopup.getX(), methodFilterPopup.getY()));
        group.add(filterAction);

        ActionToolbar topToolBar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true);
        topToolBar.setTargetComponent(this);
        return topToolBar;
    }

    private void refreshTree(boolean isExpand) {
        DumbService.getInstance(project).smartInvokeLater(
                () -> {
                    HttpApiTreePanel httpApiTreePanel = requestPanel.getHttpApiTreePanel();
                    List<HttpEnum.HttpMethod> selectedValues = methodFilterPopup.getSelectedValues();
                    List<String> nodeShowValues = nodeShowFilterPopup.getSelectedValues();
                    ReadAction.nonBlocking(() -> httpApiTreePanel.initNodes(selectedValues, nodeShowValues))
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


    @Description("创建导出操作菜单组")
    private DefaultActionGroup createExportActionGroup() {
        DefaultActionGroup exportGroup = new DefaultActionGroup(Bundle.get("http.action.group.export.env"), true);
        exportGroup.getTemplatePresentation().setIcon(HttpIcons.General.EXPORT);
        HttpConfigExportAction exportOne = new HttpConfigExportAction(Bundle.get("http.action.export.current.env"), HttpEnum.ExportEnum.SPECIFY_ENV);
        exportOne.initAction(null, null, null);
        exportGroup.add(exportOne);

        HttpConfigExportAction exportAll = new HttpConfigExportAction(Bundle.get("http.action.export.all.env"), HttpEnum.ExportEnum.ALL_ENV);
        exportAll.initAction(null, null, null);
        exportGroup.add(exportAll);

        HttpConfigExportAction exportAllApi = new HttpConfigExportAction(Bundle.get("http.action.export.all.api"), HttpEnum.ExportEnum.API);
        HttpApiTreePanel treePanel = requestPanel.getHttpApiTreePanel();
        exportAll.initAction(treePanel.getModuleControllerMap(), treePanel.getMethodNodeMap(), null);
        exportGroup.add(exportAllApi);

        return exportGroup;
    }
}
