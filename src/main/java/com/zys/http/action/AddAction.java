package com.zys.http.action;

import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-03
 */
@Description("添加菜单")
public class AddAction extends CustomAction {
    public AddAction(String text, String description) {
        super(text, description, HttpIcons.General.ADD);

    }
}
