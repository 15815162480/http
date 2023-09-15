package com.zys.http.action;

import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-14
 */
@Description("展开操作")
public class ExpandAction extends CustomAction{
    public ExpandAction() {
        super("全部展开", "Expand all", HttpIcons.General.EXPAND);
    }
}
