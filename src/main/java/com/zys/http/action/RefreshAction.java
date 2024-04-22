package com.zys.http.action;

import com.intellij.icons.AllIcons;
import com.zys.http.extension.service.Bundle;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-14
 */
@Description("刷新操作")
public class RefreshAction extends CustomAction {
    public RefreshAction() {
        super(Bundle.get("http.common.action.refresh"), "Refresh", AllIcons.Actions.Refresh);
    }
}
