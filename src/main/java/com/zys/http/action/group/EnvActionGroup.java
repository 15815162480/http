package com.zys.http.action.group;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.zys.http.action.AddAction;
import com.zys.http.action.CommonAction;
import com.zys.http.action.ExportAction;
import com.zys.http.entity.HttpConfig;
import com.zys.http.service.Bundle;
import com.zys.http.service.NotifyService;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.tool.velocity.VelocityTool;
import com.zys.http.ui.dialog.EnvAddOrEditDialog;
import com.zys.http.ui.dialog.EnvListShowDialog;
import com.zys.http.ui.icon.HttpIcons;
import com.zys.http.ui.tree.HttpApiTreePanel;
import com.zys.http.ui.window.panel.RequestPanel;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

/**
 * @author zhou ys
 * @since 2023-09-14
 */
@Description("环境相关操作菜单组")
public class EnvActionGroup extends DefaultActionGroup {
    private final RequestPanel requestPanel;

    public EnvActionGroup(RequestPanel requestPanel) {
        super(Bundle.get("http.action.group.env"), "Env", HttpIcons.General.ENVIRONMENT);
        setPopup(true);
        this.requestPanel = requestPanel;
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        if (Objects.isNull(e) || Objects.isNull(e.getProject())) {
            return new AnAction[]{};
        }
        AnAction[] actions = new AnAction[4];
        AddAction addAction = new AddAction(Bundle.get("http.action.add.env"));
        addAction.setAction(event -> new EnvAddOrEditDialog(e.getProject(), true, "", null).show());
        actions[0] = addAction;
        SelectActionGroup selectActionGroup = new SelectActionGroup();
        selectActionGroup.setPopup(true);
        selectActionGroup.setCallback(s -> requestPanel.reload(requestPanel.getHttpApiTreePanel().getChooseNode()));
        actions[1] = selectActionGroup;
        CommonAction envListAction = new CommonAction(Bundle.get("http.action.show.env"), "Env list", null);
        envListAction.setAction(event -> new EnvListShowDialog(requestPanel).show());
        actions[2] = envListAction;
        actions[3] = createExportActionGroup();

        return actions;
    }

    @Description("创建导出操作菜单组")
    private DefaultActionGroup createExportActionGroup() {
        HttpServiceTool serviceTool = requestPanel.getServiceTool();
        Project project = requestPanel.getProject();
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
