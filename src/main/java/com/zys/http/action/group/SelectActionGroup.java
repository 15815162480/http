package com.zys.http.action.group;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.zys.http.action.SelectAction;
import com.zys.http.service.Bundle;
import com.zys.http.tool.HttpPropertyTool;
import com.zys.http.ui.icon.HttpIcons;
import com.zys.http.ui.window.panel.RequestPanel;
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

    private final RequestPanel requestPanel;

    public SelectActionGroup(RequestPanel requestPanel){
        super(Bundle.get("http.action.group.select"), "Select env", HttpIcons.General.LIST);
        setPopup(true);
        this.requestPanel = requestPanel;
    }

    @Override
    @Description("实现动态菜单的关键方法")
    public AnAction @NotNull [] getChildren(AnActionEvent e) {
        HttpPropertyTool tool = HttpPropertyTool.getInstance(Objects.requireNonNull(e).getProject());
        Set<String> set = tool.getHttpConfigs().keySet();
        AnAction[] anActions = new AnAction[set.size()];

        int i = 0;
        for (String s : set) {
            SelectAction action = new SelectAction(s, s);
            action.setAction(event -> {
                event.getPresentation().setIcon(HttpIcons.General.ADD);
                tool.setSelectedEnv(event.getPresentation().getText());
                requestPanel.reload(requestPanel.getHttpApiTreePanel().getChooseNode());
            });
            if (s.equals(tool.getSelectedEnv())) {
                action.setIcon(HttpIcons.General.DEFAULT);
            }
            anActions[i++] = action;
        }
        return anActions;
    }

}
