package com.zys.http.ui.window;

import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.messages.MessageBusConnection;
import com.zys.http.action.*;
import com.zys.http.action.group.ApiToolSettingActionGroup;
import com.zys.http.action.group.EnvActionGroup;
import com.zys.http.action.group.NodeFilterActionGroup;
import com.zys.http.action.group.SelectActionGroup;
import com.zys.http.constant.HttpEnum;
import com.zys.http.service.Bundle;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.tool.SystemTool;
import com.zys.http.tool.ui.ThemeTool;
import com.zys.http.ui.dialog.EnvAddOrEditDialog;
import com.zys.http.ui.icon.HttpIcons;
import com.zys.http.ui.popup.MethodFilterPopup;
import com.zys.http.ui.popup.NodeShowFilterPopup;
import com.zys.http.ui.tree.HttpApiTreePanel;
import com.zys.http.ui.window.panel.RequestPanel;
import jdk.jfr.Description;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
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

    @Setter
    @Description("是否生成默认")
    private transient Runnable generateDefaultCb;

    private final transient ExecutorService executorTaskBounded = new ThreadPoolExecutor(
            1,
            1,
            5L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );

    public RequestTabWindow(@NotNull Project project) {
        super(true, true);
        this.project = project;
        this.requestPanel = new RequestPanel(project);
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

        MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect(this);
        connection.subscribe(LafManagerListener.TOPIC, (LafManagerListener) lafManager -> {
            setToolbar(null);
            init();
        });
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

        // 环境操作菜单组
        EnvActionGroup envActionGroup = new EnvActionGroup();
        AddAction addAction = new AddAction(Bundle.get("http.action.add.env"));
        addAction.setAction(event -> new EnvAddOrEditDialog(project, true, "").show());
        envActionGroup.add(addAction);
        SelectActionGroup selectActionGroup = new SelectActionGroup();
        selectActionGroup.setCallback(s -> requestPanel.reload(requestPanel.getHttpApiTreePanel().getChooseNode()));
        envActionGroup.add(selectActionGroup);

        group.add(envActionGroup);

        // 刷新菜单
        RefreshAction refreshAction = new RefreshAction();
        refreshAction.setAction(event -> {
            requestPanel.reload(null);
            requestPanel.getHttpApiTreePanel().clear();
            SystemTool.schedule(() -> refreshTree(false), 500);
        });
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
        NodeFilterActionGroup filterActionGroup = new NodeFilterActionGroup();
        FilterAction settingAction = new FilterAction(Bundle.get("http.filter.action.node.show"));
        settingAction.setAction(e -> nodeShowFilterPopup.show(requestPanel, nodeShowFilterPopup.getX(), nodeShowFilterPopup.getY()));
        filterActionGroup.add(settingAction);
        FilterAction filterAction = new FilterAction(Bundle.get("http.filter.action"));
        filterAction.setAction(e -> methodFilterPopup.show(requestPanel, methodFilterPopup.getX(), methodFilterPopup.getY()));
        filterActionGroup.add(filterAction);

        group.add(filterActionGroup);


        // 设置菜单组
        ApiToolSettingActionGroup settingActionGroup = new ApiToolSettingActionGroup();
        HttpServiceTool serviceTool = requestPanel.getServiceTool();
        Icon icon = ThemeTool.isDark() ? HttpIcons.General.DEFAULT : HttpIcons.General.DEFAULT_LIGHT;
        CommonAction commonAction = new CommonAction(Bundle.get("http.action.default.env"), "Generate Default",
                serviceTool.getGenerateDefault() ? icon : null);
        commonAction.setAction(event -> {
            serviceTool.refreshGenerateDefault();
            commonAction.getTemplatePresentation().setIcon(serviceTool.getGenerateDefault() ? icon : null);
            requestPanel.getHttpApiTreePanel().clear();
            SystemTool.schedule(() -> refreshTree(false), 500);
            SystemTool.schedule(() -> generateDefaultCb.run(), 600);
        });
        settingActionGroup.setCommonAction(commonAction);
        group.add(settingActionGroup);

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
        exportGroup.getTemplatePresentation().setIcon(ThemeTool.isDark() ? HttpIcons.General.OUT : HttpIcons.General.OUT_LIGHT);
        HttpConfigExportAction exportOne = new HttpConfigExportAction(Bundle.get("http.action.export.current.env"), HttpEnum.ExportEnum.SPECIFY_ENV);
        exportOne.initAction(null, null, null);
        exportGroup.add(exportOne);

        HttpConfigExportAction exportAll = new HttpConfigExportAction(Bundle.get("http.action.export.all.env"), HttpEnum.ExportEnum.ALL_ENV);
        exportAll.initAction(null, null, null);
        exportGroup.add(exportAll);

        HttpConfigExportAction exportAllApi = new HttpConfigExportAction(Bundle.get("http.action.export.all.api"), HttpEnum.ExportEnum.API);
        HttpApiTreePanel treePanel = requestPanel.getHttpApiTreePanel();
        exportAllApi.initAction(treePanel.getModuleControllerMap(), treePanel.getMethodNodeMap(), null);
        exportGroup.add(exportAllApi);

        return exportGroup;
    }
}
