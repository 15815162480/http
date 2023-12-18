package com.zys.http.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import jdk.jfr.Description;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author zys
 * @since 2023-09-03
 */
@Description("自定义菜单操作")
public abstract class CustomAction extends AnAction {

    @Setter
    protected Consumer<AnActionEvent> action;

    @Setter
    private boolean isEnabled = true;

    protected CustomAction(String text, String description, Icon icon) {
        super(text, description, icon);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        if (Objects.nonNull(action)) {
            action.accept(anActionEvent);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(isEnabled);
    }
}
