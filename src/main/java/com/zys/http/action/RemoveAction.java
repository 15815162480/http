package com.zys.http.action;

import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-03
 */
@Description("删除操作")
public class RemoveAction extends CustomAction {
    public RemoveAction(String text, String description) {
        super(text, description, HttpIcons.General.REMOVE);
    }
}
