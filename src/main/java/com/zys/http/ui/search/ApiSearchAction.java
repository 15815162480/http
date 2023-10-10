package com.zys.http.ui.search;

import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.*;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.zys.http.constant.HttpEnum;
import com.zys.http.service.Bundle;
import com.zys.http.service.configuration.ApiSearchConfiguration;
import com.zys.http.tool.ui.ThemeTool;
import com.zys.http.ui.icon.HttpIcons;
import com.zys.http.ui.tree.node.MethodNode;
import jdk.jfr.Description;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author zhou ys
 * @since 2023-10-10
 */
@Description("搜索操作")
public class ApiSearchAction extends GotoActionBase {

    @Setter
    private List<MethodNode> methodNodeList;

    public ApiSearchAction(String text) {
        getTemplatePresentation().setText(text);
        getTemplatePresentation().setDescription("Search");
        getTemplatePresentation().setIcon(ThemeTool.isDark() ? HttpIcons.General.SEARCH : HttpIcons.General.SEARCH_LIGHT);
    }

    @Override
    protected void gotoActionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.popup.service");

        ChooseByNameContributor[] contributors = {new ApiSearchChooseByNameContributor(methodNodeList)};
        ApiSearchFilteringGotoByModel model = new ApiSearchFilteringGotoByModel(project, contributors);
        GotoActionCallback<HttpEnum.HttpMethod> callback = new GotoActionCallback<>() {

            @NotNull
            @Override
            protected ChooseByNameFilter<HttpEnum.HttpMethod> createFilter(@NotNull ChooseByNamePopup popup) {
                return new GotoRequestMappingFilter(popup, model, project);
            }

            @Override
            public void elementChosen(ChooseByNamePopup chooseByNamePopup, Object element) {
                if (element instanceof ApiSearchItem navigationItem && (navigationItem.canNavigate())) {
                    navigationItem.navigate(true);
                }
            }
        };
        DefaultChooseByNameItemProvider provider = new DefaultChooseByNameItemProvider(getPsiContext(e));
        showNavigationPopup(
                e, model, callback,
                Bundle.get("http.action.search"),
                true,
                true,
                (ChooseByNameItemProvider) provider
        );
    }

    @Override
    protected <T> void showNavigationPopup(@NotNull AnActionEvent e,
                                           @NotNull ChooseByNameModel model,
                                           final GotoActionCallback<T> callback,
                                           @Nullable final String findUsagesTitle,
                                           boolean useSelectionFromEditor,
                                           final boolean allowMultipleSelection,
                                           final ChooseByNameItemProvider itemProvider) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        // noinspection ConstantConditions
        boolean mayRequestOpenInCurrentWindow = model.willOpenEditor() &&
                FileEditorManagerEx.getInstanceEx(project).hasSplitOrUndockedWindows();
        Pair<String, Integer> start = getInitialText(useSelectionFromEditor, e);
        showNavigationPopup(callback, findUsagesTitle,
                ApiSearchChooseByNamePopup.createPopup(project, model, itemProvider, start.first,
                        mayRequestOpenInCurrentWindow,
                        start.second),
                allowMultipleSelection);
    }

    protected static class GotoRequestMappingFilter extends ChooseByNameFilter<HttpEnum.HttpMethod> {

        GotoRequestMappingFilter(final ChooseByNamePopup popup, ApiSearchFilteringGotoByModel model, final Project project) {
            super(popup, model, ApiSearchConfiguration.getInstance(project), project);
        }

        @Override
        @NotNull
        protected List<HttpEnum.HttpMethod> getAllFilterValues() {
            return Arrays.asList(HttpEnum.HttpMethod.values());
        }

        @Override
        protected String textForFilterValue(@NotNull HttpEnum.HttpMethod value) {
            return value.name();
        }

        @Override
        protected Icon iconForFilterValue(@NotNull HttpEnum.HttpMethod value) {
            return HttpIcons.HttpMethod.getHttpMethodIcon(value);
        }
    }
}
