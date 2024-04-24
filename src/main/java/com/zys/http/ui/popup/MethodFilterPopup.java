package com.zys.http.ui.popup;

import com.intellij.openapi.project.Project;
import com.zys.http.constant.HttpEnum;

import java.util.Arrays;
import java.util.List;

/**
 * @author zhou ys
 * @since 2023-09-21
 */
public class MethodFilterPopup extends AbstractFilterPopup<HttpEnum.HttpMethod> {
    private static final List<HttpEnum.HttpMethod> METHODS = Arrays.stream(HttpEnum.HttpMethod.values())
            .filter(o -> !o.equals(HttpEnum.HttpMethod.REQUEST))
            .toList();
    public MethodFilterPopup(Project project) {
        super(project, METHODS);
    }

    public MethodFilterPopup(Project project,List<HttpEnum.HttpMethod> selectedValues) {
        super(project, METHODS, selectedValues);
    }
}
