package com.zys.http.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * @author zys
 * @since 2023-09-03
 */
public abstract class CustomAction extends AnAction {

    @Setter
    protected Consumer<AnActionEvent> action;

    @Setter
    private boolean isEnabled = true;

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
    protected CustomAction(String text, String description, Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        if (this.action != null) {
            action.accept(anActionEvent);
        }
    }
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(isEnabled);
    }
}
