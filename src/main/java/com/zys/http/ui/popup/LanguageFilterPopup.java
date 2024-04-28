package com.zys.http.ui.popup;

import com.intellij.openapi.project.Project;
import com.zys.http.constant.HttpEnum;

import java.util.Arrays;
import java.util.List;

/**
 * @author zhou ys
 * @since 2024-04-28
 */
public class LanguageFilterPopup extends AbstractFilterPopup<HttpEnum.Language> {
    private static final List<HttpEnum.Language> LANGUAGES = Arrays.stream(HttpEnum.Language.values()).toList();
    public LanguageFilterPopup(Project project) {
        super(project, LANGUAGES);
    }

    public LanguageFilterPopup(Project project, List<HttpEnum.Language> selectedValues) {
        super(project, LANGUAGES, selectedValues);
    }
}
