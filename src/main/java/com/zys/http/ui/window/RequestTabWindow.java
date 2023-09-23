package com.zys.http.ui.window;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.zys.http.action.*;
import com.zys.http.action.group.EnvActionGroup;
import com.zys.http.action.group.SelectActionGroup;
import com.zys.http.constant.HttpEnum;
import com.zys.http.entity.HttpConfig;
import com.zys.http.service.Bundle;
import com.zys.http.service.NotifyService;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.tool.velocity.VelocityTool;
import com.zys.http.ui.dialog.EnvAddOrEditDialog;
import com.zys.http.ui.dialog.EnvListShowDialog;
import com.zys.http.ui.icon.HttpIcons;
import com.zys.http.ui.popup.MethodFilterPopup;
import com.zys.http.ui.tree.HttpApiTreePanel;
import com.zys.http.ui.window.panel.RequestPanel;
import jdk.jfr.Description;

import java.io.IOException;
import java.util.*;
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
            dialog.getEnvShowTable().setEditOKCb(n-> requestPanel.reload(requestPanel.getHttpApiTreePanel().getChooseNode()));
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


    @Description("创建导出操作菜单组")
    private DefaultActionGroup createExportActionGroup() {
        HttpServiceTool serviceTool = requestPanel.getServiceTool();
        DefaultActionGroup exportGroup = new DefaultActionGroup(Bundle.get("http.action.group.export.env"), true);
        exportGroup.getTemplatePresentation().setIcon(HttpIcons.General.EXPORT);
        ExportAction exportOne = new ExportAction(Bundle.get("http.action.export.current.env"));
        exportOne.setAction(event -> {
            VirtualFile selectedFile = null;
            try {
                HttpConfig config = serviceTool.getDefaultHttpConfig();
                selectedFile = createFileChooser(project);
                String path = selectedFile.getPath();
                VelocityTool.exportEnv(serviceTool.getSelectedEnv(), config, path);
                exportSuccess(project);
            } catch (IOException ex) {
                if (Objects.nonNull(selectedFile)) {
                    exportFail(project);
                }
            }
        });
        exportGroup.add(exportOne);

        ExportAction exportAll = new ExportAction(Bundle.get("http.action.export.all.env"));
        exportAll.setAction(event -> {
            VirtualFile selectedFile = null;
            try {
                selectedFile = createFileChooser(project);
                String path = selectedFile.getPath();
                VelocityTool.exportAllEnv(serviceTool.getHttpConfigs(), path);
                exportSuccess(project);
            } catch (IOException ex) {
                if (Objects.nonNull(selectedFile)) {
                    exportFail(project);
                }
            }
        });
        exportGroup.add(exportAll);

        ExportAction exportAllApi = new ExportAction(Bundle.get("http.action.export.all.api"));
        exportAll.setAction(event -> {
            VirtualFile selectedFile = null;
            try {
                selectedFile = createFileChooser(project);
                String path = selectedFile.getPath();
                HttpApiTreePanel treePanel = requestPanel.getHttpApiTreePanel();
                VelocityTool.exportAllModuleApi(treePanel.getModuleControllerMap(), treePanel.getMethodNodeMap(), path);
                exportSuccess(project);
            } catch (IOException ex) {
                if (Objects.nonNull(selectedFile)) {
                    exportFail(project);
                }
            }
        });
        exportGroup.add(exportAllApi);


        return exportGroup;
    }

    @Description("创建文件选择对话框")
    private VirtualFile createFileChooser(Project project) throws IOException {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        descriptor.setTitle(Bundle.get("http.dialog.env.export"));
        FileChooserFactory.getInstance().createFileChooser(descriptor, project, requestPanel);
        VirtualFile selectedFile = FileChooser.chooseFile(descriptor, project, null);
        if (Objects.isNull(selectedFile)) {
            NotifyService.instance(project).error("http.message.export.unselect.folder");
            throw new IOException("A");
        }
        return selectedFile;
    }

    private void exportSuccess(Project project) {
        NotifyService.instance(project).info(Bundle.get("http.message.export.success"));
    }

    private void exportFail(Project project) {
        NotifyService.instance(project).error(Bundle.get("http.message.export.fail"));
    }
}
