package com.zys.http.window.request;

import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vcs.BranchChangeListener;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.zys.http.action.*;
import com.zys.http.action.group.SelectActionGroup;
import com.zys.http.constant.HttpEnum;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.setting.HttpSetting;
import com.zys.http.extension.topic.EnvironmentTopic;
import com.zys.http.extension.topic.TreeTopic;
import com.zys.http.tool.ui.ThemeTool;
import com.zys.http.ui.dialog.EnvAddOrEditDialog;
import com.zys.http.ui.icon.HttpIcons;
import com.zys.http.ui.popup.MethodFilterPopup;
import com.zys.http.ui.popup.NodeShowFilterPopup;
import com.zys.http.window.request.panel.RequestPanel;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * @author zhou ys
 * @since 2024-04-16
 */
public class RequestWindow extends SimpleToolWindowPanel implements Disposable {
    private final transient Project project;
    private RequestPanel requestPanel;
    @Description("请求方式过滤菜单")
    private MethodFilterPopup methodFilterPopup;
    @Description("结点展示过滤")
    private NodeShowFilterPopup nodeShowFilterPopup;

    public RequestWindow(Project project) {
        super(true, true);
        this.project = project;
        init();
    }

    private void init() {
        List<HttpEnum.HttpMethod> methods = Arrays.stream(HttpEnum.HttpMethod.values())
                .filter(o -> !o.equals(HttpEnum.HttpMethod.REQUEST))
                .toList();
        this.methodFilterPopup = new MethodFilterPopup(project, methods);
        this.nodeShowFilterPopup = new NodeShowFilterPopup(project);
        List<HttpEnum.HttpMethod> selectedValues = methodFilterPopup.getSelectedValues();
        List<String> nodeShowValues = nodeShowFilterPopup.getSelectedValues();
        this.requestPanel = new RequestPanel(project);
        this.requestPanel.loadNodes(selectedValues, nodeShowValues);
        this.setContent(requestPanel);
        initToolbar();
        initTopic();
    }

    private void initToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        // 环境操作菜单组
        DefaultActionGroup envActionGroup = new DefaultActionGroup(Bundle.get("http.api.icon.env"), true);
        envActionGroup.getTemplatePresentation().setIcon(ThemeTool.isDark() ? HttpIcons.General.ENVIRONMENT : HttpIcons.General.ENVIRONMENT_LIGHT);

        AddAction addAction = new AddAction(Bundle.get("http.api.icon.env.action.add.env"));
        addAction.setAction(event -> new EnvAddOrEditDialog(project, true, "").show());
        envActionGroup.add(addAction);
        envActionGroup.add(new SelectActionGroup());

        group.add(envActionGroup);

        // 刷新菜单
        RefreshAction refreshAction = new RefreshAction();
        refreshAction.setAction(event -> project.getMessageBus().syncPublisher(TreeTopic.REFRESH_TOPIC).refresh(false));
        group.add(refreshAction);

        // 导出菜单操作组
        group.add(createExportActionGroup());
        group.addSeparator();

        // 展开操作菜单
        ExpandAction expandAction = new ExpandAction();
        expandAction.setAction(event -> requestPanel.treeExpand());
        group.add(expandAction);

        // 收起操作菜单
        CollapseAction collapseAction = new CollapseAction();
        collapseAction.setAction(event -> requestPanel.treeCollapse());
        group.add(collapseAction);
        group.addSeparator();

        // 节点过滤操作菜单组
        DefaultActionGroup filterActionGroup = new DefaultActionGroup(Bundle.get("http.api.icon.node.filter"), true);
        filterActionGroup.getTemplatePresentation().setIcon(ThemeTool.isDark() ? HttpIcons.General.FILTER_GROUP : HttpIcons.General.FILTER_GROUP_LIGHT);
        FilterAction settingAction = new FilterAction(Bundle.get("http.api.icon.node.filter.action.node.show"));
        settingAction.setAction(e -> nodeShowFilterPopup.show(requestPanel, nodeShowFilterPopup.getX(), nodeShowFilterPopup.getY()));
        filterActionGroup.add(settingAction);
        FilterAction filterAction = new FilterAction(Bundle.get("http.api.icon.node.filter.action.method"));
        filterAction.setAction(e -> methodFilterPopup.show(requestPanel, methodFilterPopup.getX(), methodFilterPopup.getY()));
        filterActionGroup.add(filterAction);

        group.add(filterActionGroup);

        ActionToolbar topToolBar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true);
        topToolBar.setTargetComponent(this);

        setToolbar(topToolBar.getComponent());
    }

    @Description("创建导出操作菜单组")
    private @NotNull DefaultActionGroup createExportActionGroup() {
        DefaultActionGroup exportGroup = new DefaultActionGroup(Bundle.get("http.api.icon.postman"), true);
        exportGroup.getTemplatePresentation().setIcon(ThemeTool.isDark() ? HttpIcons.General.OUT : HttpIcons.General.OUT_LIGHT);

        HttpConfigExportAction exportOne = new HttpConfigExportAction(Bundle.get("http.api.icon.postman.action.export.current.env"), HttpEnum.ExportEnum.SPECIFY_ENV);
        exportOne.initAction(null);
        exportGroup.add(exportOne);

        HttpConfigExportAction exportAll = new HttpConfigExportAction(Bundle.get("http.api.icon.postman.action.export.all.env"), HttpEnum.ExportEnum.ALL_ENV);
        exportAll.initAction(null);
        exportGroup.add(exportAll);

        HttpConfigExportAction exportAllApi = new HttpConfigExportAction(Bundle.get("http.api.icon.postman.action.export.all.api"), HttpEnum.ExportEnum.API);
        exportAllApi.initAction(null);
        exportGroup.add(exportAllApi);

        return exportGroup;
    }

    @Description("监听事件通知")
    private void initTopic() {
        MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect(this);
        connection.subscribe(LafManagerListener.TOPIC, (LafManagerListener) lafManager -> {
            setToolbar(null);
            init();
            List<HttpEnum.HttpMethod> methods = Arrays.stream(HttpEnum.HttpMethod.values())
                    .filter(o -> !o.equals(HttpEnum.HttpMethod.REQUEST))
                    .toList();
            this.methodFilterPopup = new MethodFilterPopup(project, methods);
            this.nodeShowFilterPopup = new NodeShowFilterPopup(project);
        });

        MessageBus messageBus = project.getMessageBus();
        MessageBusConnection connect = messageBus.connect();
        connect.subscribe(TreeTopic.REFRESH_TOPIC, (TreeTopic.Refresh) b -> {
            requestPanel.clearApiTree();
            requestPanel.reload(null);
            refreshTree(b);
        });

        project.getMessageBus().connect().subscribe(EnvironmentTopic.CHANGE_TOPIC,
                (EnvironmentTopic.Change) () -> requestPanel.reload(requestPanel.getApiTreeChooseNode()));

        project.getMessageBus().connect().subscribe(BranchChangeListener.VCS_BRANCH_CHANGED, new BranchChangeListener() {
            @Override
            public void branchWillChange(@NotNull String branchName) {
                if (HttpSetting.getInstance().getRefreshWhenVcsChange()) {
                    requestPanel.clearApiTree();
                }
            }

            @Override
            public void branchHasChanged(@NotNull String branchName) {
                if (HttpSetting.getInstance().getRefreshWhenVcsChange()) {
                    requestPanel.reload(null);
                    refreshTree(false);
                }
            }
        });
    }

    private void refreshTree(boolean isExpand) {
        DumbService.getInstance(project).runWhenSmart(() -> {
            List<HttpEnum.HttpMethod> selectedValues = methodFilterPopup.getSelectedValues();
            List<String> nodeShowValues = nodeShowFilterPopup.getSelectedValues();
            requestPanel.loadNodes(selectedValues, nodeShowValues);
            if (isExpand) {
                requestPanel.treeExpandAll();
            }
        });
    }

    @Override
    public void dispose() {
        // 不处理
    }
}
