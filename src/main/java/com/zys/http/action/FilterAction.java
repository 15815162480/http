package com.zys.http.action;

import com.intellij.icons.AllIcons;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-20
 */
@Description("过滤操作")
public class FilterAction extends CustomAction {
    public FilterAction(String text) {
        super(text, "Filter", AllIcons.General.Filter);
    }
}
