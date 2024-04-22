package com.zys.http.action;

import com.intellij.icons.AllIcons;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-03
 */
@Description("删除操作")
public class RemoveAction extends CustomAction {
    public RemoveAction(String text) {
        super(text, "Remove", AllIcons.General.Remove);
    }
}
