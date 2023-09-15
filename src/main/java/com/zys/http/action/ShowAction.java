package com.zys.http.action;

import jdk.jfr.Description;

import javax.swing.*;

/**
 * @author zhou ys
 * @since 2023-09-05
 */
@Description("展示操作")
public class ShowAction extends CustomAction {
    public ShowAction(String text, String description, Icon icon) {
        super(text, description, icon);
    }
}
