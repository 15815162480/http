package com.zys.http.action.group;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.zys.http.action.SelectAction;
import com.zys.http.service.Bundle;
import com.zys.http.service.topic.EnvChangeTopic;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.tool.ui.ThemeTool;
import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

/**
 * @author zhou ys
 * @since 2023-09-13
 */
@Description("选择环境菜单组")
public class SelectActionGroup extends DefaultActionGroup {

    public SelectActionGroup() {
        super(Bundle.get("http.action.group.select.env"), "Select env", HttpIcons.General.TREE);
        setPopup(true);
    }

    @Override
    @Description("实现动态菜单的关键方法")
    public AnAction @NotNull [] getChildren(AnActionEvent e) {
        if (Objects.isNull(e) || Objects.isNull(e.getProject())) {
            return new AnAction[0];
        }
        HttpServiceTool tool = HttpServiceTool.getInstance(e);
        Set<String> set = tool.getHttpConfigs().keySet();
        AnAction[] anActions = new AnAction[set.size()];
        Project project = e.getProject();

        int i = 0;
        for (String s : set) {
            SelectAction action = new SelectAction(s);
            action.setAction(event -> {
                tool.setSelectedEnv(event.getPresentation().getText());
                project.getMessageBus().syncPublisher(EnvChangeTopic.TOPIC).change();
            });
            if (s.equals(tool.getSelectedEnv())) {
                action.setIcon(ThemeTool.isDark() ? HttpIcons.General.DEFAULT : HttpIcons.General.DEFAULT_LIGHT);
            }
            anActions[i++] = action;
        }
        return anActions;
    }

}
