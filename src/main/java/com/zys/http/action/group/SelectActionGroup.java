package com.zys.http.action.group;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.zys.http.action.SelectAction;
import com.zys.http.service.Bundle;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author zhou ys
 * @since 2023-09-13
 */
@Description("选择环境菜单组")
public class SelectActionGroup extends DefaultActionGroup {


    @Setter
    private Consumer<String> callback;

    public SelectActionGroup() {
        super(Bundle.get("http.action.group.select.env"), "Select env", HttpIcons.General.LIST);
        setPopup(true);
    }

    @Override
    @Description("实现动态菜单的关键方法")
    public AnAction @NotNull [] getChildren(AnActionEvent e) {
        HttpServiceTool tool = HttpServiceTool.getInstance(Objects.requireNonNull(Objects.requireNonNull(e).getProject()));
        Set<String> set = tool.getHttpConfigs().keySet();
        AnAction[] anActions = new AnAction[set.size()];

        int i = 0;
        for (String s : set) {
            SelectAction action = new SelectAction(s);
            action.setAction(event -> {
                String selectEnv = event.getPresentation().getText();
                tool.setSelectedEnv(selectEnv);
                callback.accept(selectEnv);
            });
            if (s.equals(tool.getSelectedEnv())) {
                action.setIcon(HttpIcons.General.DEFAULT);
            }
            anActions[i++] = action;
        }
        return anActions;
    }

}
