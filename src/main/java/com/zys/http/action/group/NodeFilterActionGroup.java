package com.zys.http.action.group;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.zys.http.service.Bundle;
import com.zys.http.ui.icon.HttpIcons;

/**
 * @author zhou ys
 * @since 2023-09-26
 */
public class NodeFilterActionGroup extends DefaultActionGroup {

    public NodeFilterActionGroup() {
        getTemplatePresentation().setIcon(HttpIcons.General.FILTER);
        getTemplatePresentation().setText(Bundle.get("http.filter.action.node.filter"));
        setPopup(true);
    }
}
