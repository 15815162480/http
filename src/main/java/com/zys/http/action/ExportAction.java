package com.zys.http.action;

import com.zys.http.tool.ui.ThemeTool;
import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-23
 */
@Description("导出操作")
public class ExportAction extends CommonAction {
    public ExportAction(String text) {
        super(text, "Export",
                ThemeTool.isDark() ? HttpIcons.General.EXPORT : HttpIcons.General.EXPORT_LIGHT);
    }
}
