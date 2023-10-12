package com.zys.http.action;

import com.zys.http.tool.ui.ThemeTool;
import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-20
 */
@Description("过滤操作")
public class FilterAction extends CustomAction {
    public FilterAction(String text) {
        super(text, "Filter",
                ThemeTool.isDark() ? HttpIcons.General.FILTER : HttpIcons.General.FILTER_LIGHT);
    }
}
