package com.zys.http.action;

import jdk.jfr.Description;

import javax.swing.*;

/**
 * @author zys
 * @since 2023-09-13
 */
@Description("选择操作")
public class SelectAction extends CustomAction {
    public SelectAction(String text) {
        super(text, "Select", null);
    }

    public void setIcon(Icon icon) {
        getTemplatePresentation().setIcon(icon);
    }
}
