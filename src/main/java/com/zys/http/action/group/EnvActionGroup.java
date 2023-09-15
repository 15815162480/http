package com.zys.http.action.group;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.zys.http.action.AddAction;
import com.zys.http.action.ShowAction;
import com.zys.http.ui.dialog.EnvAddOrEditDialog;
import com.zys.http.ui.dialog.EnvListShowDialog;
import com.zys.http.ui.icon.HttpIcons;
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

    public EnvActionGroup() {
        super("环境", "Env", HttpIcons.General.ENVIRONMENT);
        setPopup(true);
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        if (Objects.isNull(e) || Objects.isNull(e.getProject())) {
            return new AnAction[]{};
        }
        Project project = e.getProject();
        AnAction[] actions = new AnAction[3];
        AddAction addAction = new AddAction("新增环境", "Add env");
        addAction.setAction(event -> new EnvAddOrEditDialog(project, true, "", null).show());
        actions[0] = addAction;
        ShowAction action = new ShowAction("环境列表", "Env list", null);
        action.setAction(event -> new EnvListShowDialog(project).show());
        actions[1] = action;
        SelectActionGroup selectActionGroup = new SelectActionGroup();
        selectActionGroup.setPopup(true);
        actions[2] = selectActionGroup;
        return actions;
    }
}
