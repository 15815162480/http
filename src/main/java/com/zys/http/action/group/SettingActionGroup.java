package com.zys.http.action.group;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.zys.http.action.CommonAction;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.topic.RefreshTreeTopic;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.tool.ui.ThemeTool;
import com.zys.http.ui.icon.HttpIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * @author zhou ys
 * @since 2023-11-13
 */
public class SettingActionGroup extends DefaultActionGroup {
    public SettingActionGroup() {
        super(Bundle.get("http.action.group.setting"), "Settings",
                ThemeTool.isDark() ? HttpIcons.General.SETTING : HttpIcons.General.SETTING_LIGHT);
        setPopup(true);
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        if (Objects.isNull(e)){
            return new AnAction[]{};
        }
        Project project = e.getProject();
        if (Objects.isNull(project)){
            return new AnAction[]{};
        }
        HttpServiceTool serviceTool = HttpServiceTool.getInstance(project);
        Icon icon = ThemeTool.isDark() ? HttpIcons.General.DEFAULT : HttpIcons.General.DEFAULT_LIGHT;
        CommonAction commonAction = new CommonAction(Bundle.get("http.action.default.env"), "Generate Default",
                serviceTool.getGenerateDefault() ? icon : null);
        commonAction.setAction(event -> {
            serviceTool.refreshGenerateDefault();
            ApplicationManager.getApplication().invokeLater(() -> {
                commonAction.getTemplatePresentation().setIcon(serviceTool.getGenerateDefault() ? icon : null);
                project.getMessageBus().syncPublisher(RefreshTreeTopic.TOPIC).refresh(false);
            });
        });
        CommonAction commonAction2 = new CommonAction(Bundle.get("http.action.vcs.change"), "Vcs Change",
                serviceTool.getRefreshWhenVcsChange() ? icon : null);
        commonAction2.setAction(event -> {
            serviceTool.refreshWhenVcsChange();
            ApplicationManager.getApplication().invokeLater(() ->
                    commonAction2.getTemplatePresentation().setIcon(serviceTool.getRefreshWhenVcsChange() ? icon : null)
            );
        });

        return new AnAction[]{commonAction,commonAction2};
    }
}
