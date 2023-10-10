package com.zys.http.ui.search;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.gotoByName.CustomMatcherModel;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.zys.http.constant.HttpEnum;
import com.zys.http.service.Bundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author zhou ys
 * @since 2023-10-10
 */
public class ApiSearchFilteringGotoByModel extends FilteringGotoByModel<HttpEnum.HttpMethod> implements DumbAware, CustomMatcherModel {
    public ApiSearchFilteringGotoByModel(@NotNull Project project, ChooseByNameContributor @NotNull [] contributors) {
        super(project, contributors);
    }

    @Override
    protected @Nullable HttpEnum.HttpMethod filterValueFor(NavigationItem item) {
        if (item instanceof ApiSearchItem apiItem) {
            return apiItem.getMethodNodeData().getHttpMethod();
        }
        return null;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) String getPromptText() {
        return Bundle.get("http.action.search");
    }

    @Override
    public @NotNull String getNotInMessage() {
        return IdeBundle.message("label.no.matches.found", getProject().getName());
    }

    @Override
    public @NotNull String getNotFoundMessage() {
        return IdeBundle.message("label.no.matches.found");
    }

    @Override
    public @Nullable String getCheckBoxName() {
        return null;
    }

    @Override
    public boolean loadInitialCheckBoxState() {
        return false;
    }

    @Override
    public void saveInitialCheckBoxState(boolean state) {
        // 不处理
    }

    @Override
    public String @NotNull [] getSeparators() {
        return new String[]{"/"};
    }

    @Override
    public @Nullable String getFullName(@NotNull Object element) {
        return getElementName(element);
    }

    @Override
    public boolean willOpenEditor() {
        return true;
    }

    @Override
    public boolean matches(@NotNull String popupItem, @NotNull String userPattern) {
        return true;
    }
}
