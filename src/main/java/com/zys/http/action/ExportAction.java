package com.zys.http.action;

import com.intellij.icons.AllIcons;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-23
 */
@Description("导出操作")
public class ExportAction extends CommonAction {
    public ExportAction(String text) {
        super(text, "Export", AllIcons.ToolbarDecorator.Export);
    }
}
