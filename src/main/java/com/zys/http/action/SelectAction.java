package com.zys.http.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.zys.http.tool.HttpPropertyTool;
import com.zys.http.ui.icon.HttpIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author zys
 * @since 2023-09-13
 */
public class SelectAction extends CustomAction {
    public SelectAction(String text, String description) {
        super(text, description, null);
    }

    public void setIcon(Icon icon) {
        getTemplatePresentation().setIcon(icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        e.getPresentation().setIcon(HttpIcons.ADD);
        HttpPropertyTool tool = HttpPropertyTool.getInstance(e.getProject());
        tool.setSelectedEnv(e.getPresentation().getText());
    }
}
