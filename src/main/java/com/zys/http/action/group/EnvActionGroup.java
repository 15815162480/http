package com.zys.http.action.group;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.zys.http.action.AddAction;
import com.zys.http.action.CommonAction;
import com.zys.http.service.Bundle;
import com.zys.http.ui.dialog.EnvAddOrEditDialog;
import com.zys.http.ui.dialog.EnvListShowDialog;
import com.zys.http.ui.icon.HttpIcons;
import com.zys.http.ui.window.panel.RequestPanel;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        Project project = e.getProject();
        AnAction[] actions = new AnAction[3];
        AddAction addAction = new AddAction(Bundle.get("http.action.add.env"), "Add env");
        addAction.setAction(event -> new EnvAddOrEditDialog(project, true, "", null).show());
        actions[0] = addAction;
        SelectActionGroup selectActionGroup = new SelectActionGroup(requestPanel);
        selectActionGroup.setPopup(true);
        actions[1] = selectActionGroup;
        CommonAction action = new CommonAction(Bundle.get("http.action.show.env"), "Env list", null);
        action.setAction(event -> new EnvListShowDialog(project, requestPanel).show());
        actions[2] = action;
        return actions;
    }
}
