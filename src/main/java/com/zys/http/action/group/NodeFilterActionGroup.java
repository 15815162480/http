package com.zys.http.action.group;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.zys.http.service.Bundle;
import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;

/**
 * @author zhou ys
 * @since 2023-09-26
 */
@Description("结点过滤操作菜单组")
public class NodeFilterActionGroup extends DefaultActionGroup {

    public NodeFilterActionGroup() {
        super(Bundle.get("http.filter.action.node.filter"), "Node filter", HttpIcons.General.FILTER_GROUP);
        setPopup(true);
    }
}
