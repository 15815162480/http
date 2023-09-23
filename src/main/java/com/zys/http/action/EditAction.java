package com.zys.http.action;

import com.intellij.icons.AllIcons;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-03
 */
@Description("编辑操作")
public class EditAction extends CustomAction {
    public EditAction(String text) {
        super(text, "Edit", AllIcons.Actions.Edit);
    }
}
