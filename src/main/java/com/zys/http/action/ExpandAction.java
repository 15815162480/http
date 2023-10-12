package com.zys.http.action;

import com.zys.http.service.Bundle;
import com.zys.http.tool.ui.ThemeTool;
import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-14
 */
@Description("展开操作")
public class ExpandAction extends CustomAction {
    public ExpandAction() {
        super(Bundle.get("http.action.expand"), "Expand",
                ThemeTool.isDark() ? HttpIcons.General.EXPAND : HttpIcons.General.EXPAND_LIGHT);
    }
}
