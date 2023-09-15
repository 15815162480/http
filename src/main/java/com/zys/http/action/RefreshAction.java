package com.zys.http.action;

import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-14
 */
@Description("刷新操作")
public class RefreshAction extends CustomAction {
    public RefreshAction() {
        super("刷新", "Refresh", HttpIcons.General.REFRESH);
    }
}
