package com.zys.http.action;

import com.zys.http.extension.service.Bundle;
import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-14
 */
@Description("刷新操作")
public class RefreshAction extends CustomAction {
    public RefreshAction() {
        super(Bundle.get("http.action.refresh"), "Refresh", HttpIcons.General.REFRESH);
    }
}
