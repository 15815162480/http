package com.zys.http.action;

import com.intellij.icons.AllIcons;
import com.zys.http.ui.icon.HttpIcons;

/**
 * @author zys
 * @since 2023-09-03
 */
public class AddAction extends CustomAction {
    public AddAction(String text, String description) {
        super(text, description, HttpIcons.ADD);
    }
}
