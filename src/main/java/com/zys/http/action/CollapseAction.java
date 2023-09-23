package com.zys.http.action;

import com.zys.http.service.Bundle;
import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-14
 */
@Description("收起操作")
public class CollapseAction extends CustomAction {
    public CollapseAction() {
        super(Bundle.get("http.action.collapse"), "Collapse", HttpIcons.General.COLLAPSE);
    }
}
