package com.zys.http.ui.popup;

import com.intellij.openapi.project.Project;
import com.zys.http.constant.HttpEnum;

import java.util.List;

/**
 * @author zhou ys
 * @since 2023-09-21
 */
public class MethodFilterPopup extends AbstractFilterPopup<HttpEnum.HttpMethod> {
    public MethodFilterPopup(Project project, List<HttpEnum.HttpMethod> values) {
        super(project, values);
    }
}
