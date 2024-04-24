package com.zys.http.ui.popup;

import com.intellij.openapi.project.Project;
import com.zys.http.extension.service.Bundle;
import jdk.jfr.Description;

import java.util.List;

/**
 * @author zhou ys
 * @since 2023-09-25
 */
@Description("结点展示弹窗")
public class NodeShowFilterPopup extends AbstractFilterPopup<String> {
    public static final List<String> SETTING_VALUES = List.of(
            Bundle.get("http.api.icon.node.filter.action.node.show.package"),
            Bundle.get("http.api.icon.node.filter.action.node.show.class")
    );

    public NodeShowFilterPopup(Project project) {
        super(project, SETTING_VALUES);
    }

    public NodeShowFilterPopup(Project project, List<String> selectedSettingValues) {
        super(project, SETTING_VALUES, selectedSettingValues);
    }
}
