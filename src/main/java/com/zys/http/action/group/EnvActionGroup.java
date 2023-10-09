package com.zys.http.action.group;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.zys.http.service.Bundle;
import com.zys.http.tool.ui.ThemeTool;
import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;

/**
 * @author zhou ys
 * @since 2023-09-14
 */
@Description("环境相关操作菜单组")
public class EnvActionGroup extends DefaultActionGroup {
    public EnvActionGroup() {
        super(Bundle.get("http.action.group.env"), "Env",
                ThemeTool.isDark() ? HttpIcons.General.ENVIRONMENT : HttpIcons.General.ENVIRONMENT_LIGHT);
        setPopup(true);
    }
}
