package com.zys.http.action;

import com.intellij.icons.AllIcons;
import com.zys.http.extension.service.Bundle;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-14
 */
@Description("收起操作")
public class CollapseAction extends CustomAction {

    public CollapseAction() {
        super(Bundle.get("http.common.action.collapse"), "Collapse", AllIcons.Actions.Collapseall);
    }
}
