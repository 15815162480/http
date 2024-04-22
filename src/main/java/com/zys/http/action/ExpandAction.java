package com.zys.http.action;

import com.intellij.icons.AllIcons;
import com.zys.http.extension.service.Bundle;
import jdk.jfr.Description;

/**
 * @author zys
 * @since 2023-09-14
 */
@Description("展开操作")
public class ExpandAction extends CustomAction {
    public ExpandAction() {
        super(Bundle.get("http.common.action.expand"), "Expand", AllIcons.Actions.Expandall);
    }
}
