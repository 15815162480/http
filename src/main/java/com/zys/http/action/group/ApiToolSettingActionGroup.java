package com.zys.http.action.group;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.zys.http.action.CommonAction;
import com.zys.http.service.Bundle;
import com.zys.http.tool.ui.ThemeTool;
import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author zhou ys
 * @since 2023-09-27
 */
@Description("插件配置的菜单组")
public class ApiToolSettingActionGroup extends DefaultActionGroup {

    @Setter
    private CommonAction commonAction;

    public ApiToolSettingActionGroup() {
        super(Bundle.get("http.action.group.setting"), "Setting",
                ThemeTool.isDark() ? HttpIcons.General.SETTING : HttpIcons.General.SETTING_LIGHT);
        setPopup(true);
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        return new AnAction[]{commonAction};
    }
}
