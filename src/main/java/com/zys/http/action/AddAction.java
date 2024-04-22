package com.zys.http.action;

import com.intellij.icons.AllIcons;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-03
 */
@Description("添加菜单")
public class AddAction extends CustomAction {
    public AddAction(String text) {
        super(text, "Add", AllIcons.General.Add);
    }
}
