package com.zys.http.action;

import com.intellij.icons.AllIcons;
import jdk.jfr.Description;

/**
 * @author zhou ys
 * @since 2023-09-26
 */
@Description("复制操作")
public class CopyAction extends CustomAction {
    public CopyAction(String text) {
        super(text, "Copy", AllIcons.Actions.Copy);
    }
}
