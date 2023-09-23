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
import com.zys.http.entity.HttpConfig;
import com.zys.http.service.Bundle;
import com.zys.http.service.NotifyService;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.tool.ui.DialogTool;
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
        HttpServiceTool serviceTool = requestPanel.getServiceTool();
        if (Objects.isNull(e) || Objects.isNull(e.getProject())) {
            return new AnAction[]{};
        }
        Project project = e.getProject();
        AnAction[] actions = new AnAction[4];
        AddAction addAction = new AddAction(Bundle.get("http.action.add.env"), "Add env");
        addAction.setAction(event -> new EnvAddOrEditDialog(project, true, "", null).show());
        actions[0] = addAction;
        SelectActionGroup selectActionGroup = new SelectActionGroup();
        selectActionGroup.setPopup(true);
        selectActionGroup.setCallback(s -> requestPanel.reload(requestPanel.getHttpApiTreePanel().getChooseNode()));
        actions[1] = selectActionGroup;
        CommonAction action = new CommonAction(Bundle.get("http.action.show.env"), "Env list", null);
        action.setAction(event -> new EnvListShowDialog(requestPanel).show());
        actions[2] = action;

        DefaultActionGroup exportGroup = new DefaultActionGroup(Bundle.get("http.action.group.export.env"), true);
        exportGroup.getTemplatePresentation().setIcon(HttpIcons.General.EXPORT);
        CommonAction exportOne = new CommonAction(Bundle.get("http.action.export.current.env"), "Export Current Env", HttpIcons.General.EXPORT);
        exportOne.setAction(event -> {
            String selectedEnv = serviceTool.getSelectedEnv();
            HttpConfig config = serviceTool.getHttpConfig(selectedEnv);
            if (Objects.isNull(config)) {
                NotifyService.instance(project).error(Bundle.get("http.message.export.current.env.error"));
                return;
            }
            VirtualFile selectedFile = createFileChooser(project);
            if (Objects.isNull(selectedFile)) {
                NotifyService.instance(project).error("http.message.export.unselect.folder");
                return;
            }
            String path = selectedFile.getPath();
            try {
                VelocityTool.exportEnv(selectedEnv, config, path);
                NotifyService.instance(project).info(Bundle.get("http.message.export.success"));
            } catch (IOException ex) {
                NotifyService.instance(project).error(Bundle.get("http.message.export.fail"));
            }
        });
        exportGroup.add(exportOne);

        CommonAction exportAll = new CommonAction(Bundle.get("http.action.export.all.env"), "Export All Env", HttpIcons.General.EXPORT);
        exportAll.setAction(event -> {
            VirtualFile selectedFile = createFileChooser(project);
            if (Objects.isNull(selectedFile)) {
                DialogTool.error("error type");
                return;
            }
            String path = selectedFile.getPath();
            try {
                VelocityTool.exportAllEnv(serviceTool.getHttpConfigs(), path);
                NotifyService.instance(project).info(Bundle.get("http.message.export.success"));
            } catch (IOException ex) {
                NotifyService.instance(project).error(Bundle.get("http.message.export.fail"));
            }
        });
        exportGroup.add(exportAll);

        CommonAction exportAllApi = new CommonAction(Bundle.get("http.action.export.all.api"), "Export All Api", HttpIcons.General.EXPORT);
        exportAll.setAction(event -> {
            VirtualFile selectedFile = createFileChooser(project);
            if (Objects.isNull(selectedFile)) {
                DialogTool.error("error type");
                return;
            }
            String path = selectedFile.getPath();
            try {
                HttpApiTreePanel treePanel = requestPanel.getHttpApiTreePanel();
                VelocityTool.exportAllModuleApi(treePanel.getModuleControllerMap(), treePanel.getMethodNodeMap(), path);
                NotifyService.instance(project).info(Bundle.get("http.message.export.success"));
            } catch (IOException ex) {
                NotifyService.instance(project).error(Bundle.get("http.message.export.fail"));
            }
        });
        exportGroup.add(exportAllApi);

        actions[3] = exportGroup;
        return actions;
    }

    @Description("创建文件选择对话框")
    private VirtualFile createFileChooser(Project project) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        descriptor.setTitle(Bundle.get("http.dialog.env.export"));
        FileChooserFactory.getInstance().createFileChooser(descriptor, project, requestPanel);
        return FileChooser.chooseFile(descriptor, project, null);
    }
}
