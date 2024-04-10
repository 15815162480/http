package com.zys.http.ui.window;

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
import com.zys.http.extension.topic.EnvChangeTopic;
import com.zys.http.extension.topic.RefreshTreeTopic;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.tool.ui.ThemeTool;
import com.zys.http.ui.dialog.EnvAddOrEditDialog;
import com.zys.http.ui.icon.HttpIcons;
import com.zys.http.ui.popup.MethodFilterPopup;
import com.zys.http.ui.popup.NodeShowFilterPopup;
import com.zys.http.ui.tree.HttpApiTreePanel;
import com.zys.http.ui.tree.node.ModuleNode;
import com.zys.http.ui.window.panel.RequestPanel;
import jdk.jfr.Description;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * @author zys
 * @since 2023-08-13
 */
@Description("请求标签页面")
public class RequestTabWindow extends SimpleToolWindowPanel implements Disposable {

    private final transient Project project;

    @Getter
    private final RequestPanel requestPanel;

    @Description("请求方式过滤菜单")
    private MethodFilterPopup methodFilterPopup;
    @Description("结点展示过滤")
    private NodeShowFilterPopup nodeShowFilterPopup;

    public RequestTabWindow(@NotNull Project project) {
        super(true, true);
        this.project = project;
        this.requestPanel = new RequestPanel(project);
        this.methodFilterPopup = new MethodFilterPopup(project,
                Arrays.stream(HttpEnum.HttpMethod.values()).filter(o -> !o.equals(HttpEnum.HttpMethod.REQUEST))
                        .toList()
        );
        this.nodeShowFilterPopup = new NodeShowFilterPopup(project);
        init();
    }

    @Description("初始化")
    public void init() {
        setContent(requestPanel);
        refreshTree(false);
        setToolbar(requestToolBar().getComponent());
        initTopic();
    }

    @Description("监听事件通知")
    private void initTopic() {
        MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect(this);
        connection.subscribe(LafManagerListener.TOPIC, (LafManagerListener) lafManager -> {
            setToolbar(null);
            init();
            this.methodFilterPopup = new MethodFilterPopup(project,
                    Arrays.stream(HttpEnum.HttpMethod.values()).filter(o -> !o.equals(HttpEnum.HttpMethod.REQUEST))
                            .toList()
            );
            this.nodeShowFilterPopup = new NodeShowFilterPopup(project);
        });

        MessageBus messageBus = project.getMessageBus();
        MessageBusConnection connect = messageBus.connect();
        connect.subscribe(RefreshTreeTopic.TOPIC, (RefreshTreeTopic) b -> {
            requestPanel.getHttpApiTreePanel().clear();
            requestPanel.reload(null);
            refreshTree(b);
        });

        project.getMessageBus().connect().subscribe(EnvChangeTopic.TOPIC,
                (EnvChangeTopic) () -> requestPanel.reload(requestPanel.getHttpApiTreePanel().getChooseNode()));

        project.getMessageBus().connect().subscribe(BranchChangeListener.VCS_BRANCH_CHANGED, new BranchChangeListener() {
            @Override
            public void branchWillChange(@NotNull String branchName) {
                if (HttpServiceTool.getInstance(project).getRefreshWhenVcsChange()) {
                    requestPanel.getHttpApiTreePanel().clear();
                }
            }

            @Override
            public void branchHasChanged(@NotNull String branchName) {
                if (HttpServiceTool.getInstance(project).getRefreshWhenVcsChange()) {
                    requestPanel.reload(null);
                    refreshTree(false);
                }
            }
        });
    }

    @Description("初始化顶部工具栏")
    private ActionToolbar requestToolBar() {
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
        refreshAction.setAction(event -> project.getMessageBus().syncPublisher(RefreshTreeTopic.TOPIC).refresh(false));
        group.add(refreshAction);

        // 导出菜单操作组
        group.add(createExportActionGroup());
        group.addSeparator();

        // 展开操作菜单
        ExpandAction expandAction = new ExpandAction();
        expandAction.setAction(event -> requestPanel.getHttpApiTreePanel().treeExpand());
        group.add(expandAction);

        // 收起操作菜单
        CollapseAction collapseAction = new CollapseAction();
        collapseAction.setAction(event -> requestPanel.getHttpApiTreePanel().treeCollapse());
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


        // 设置菜单组
        // group.add(new SettingActionGroup());

        ActionToolbar topToolBar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true);
        topToolBar.setTargetComponent(this);
        return topToolBar;
    }

    private void refreshTree(boolean isExpand) {
        DumbService.getInstance(project).runWhenSmart(
                () -> {
                    HttpApiTreePanel httpApiTreePanel = requestPanel.getHttpApiTreePanel();
                    List<HttpEnum.HttpMethod> selectedValues = methodFilterPopup.getSelectedValues();
                    List<String> nodeShowValues = nodeShowFilterPopup.getSelectedValues();
                    ModuleNode moduleNode = httpApiTreePanel.initNodes(selectedValues, nodeShowValues);
                    httpApiTreePanel.render(moduleNode);
                    if (isExpand) {
                        requestPanel.getHttpApiTreePanel().expandAll();
                    }
                }
        );
    }

    @Override
    public void dispose() {
        // 不处理
    }


    @Description("创建导出操作菜单组")
    private DefaultActionGroup createExportActionGroup() {
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
}
