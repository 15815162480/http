package com.zys.http.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.zys.http.tool.HttpPropertyTool;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

/**
 * @author zhou ys
 * @since 2023-09-13
 */
public class SelectActionGroup extends DefaultActionGroup {

    @Override
    public AnAction @NotNull [] getChildren(AnActionEvent e) {
        HttpPropertyTool tool = HttpPropertyTool.getInstance(Objects.requireNonNull(e).getProject());
        Set<String> set = tool.getHttpConfigs().keySet();
        AnAction[] anActions = new AnAction[set.size()];

        int i = 0;
        for (String s : set) {
            SelectAction action = new SelectAction(s, s);
            if (s.equals(tool.getSelectedEnv())) {
                action.setIcon(AllIcons.Actions.SetDefault);
            }
            anActions[i++] = action;
        }
        return anActions;
    }

}
