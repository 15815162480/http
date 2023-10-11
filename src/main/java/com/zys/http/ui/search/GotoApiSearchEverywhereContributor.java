package com.zys.http.ui.search;

import com.intellij.ide.actions.searcheverywhere.AbstractGotoSEContributor;
import com.intellij.ide.actions.searcheverywhere.PSIPresentationBgRendererWrapper;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author zys
 * @since 2023-10-10
 */
public class GotoApiSearchEverywhereContributor extends AbstractGotoSEContributor {
    private final GotoApiModel apiModel;

    public GotoApiSearchEverywhereContributor(@NotNull AnActionEvent event, GotoApiModel apiModel) {
        super(event);
        this.apiModel = apiModel;
    }

    @Override
    public @NotNull String getSearchProviderId() {
        return GotoApiSearchEverywhereContributor.class.getName();
    }

    @Override
    public @NotNull @Nls String getGroupName() {
        return "ApiTool";
    }

    @Override
    public int getSortWeight() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected @NotNull FilteringGotoByModel<?> createModel(@NotNull Project project) {
        return apiModel;
    }

    public static class Factory implements SearchEverywhereContributorFactory<Object> {
        @Override
        public @NotNull SearchEverywhereContributor<Object> createContributor(@NotNull AnActionEvent initEvent) {
            return PSIPresentationBgRendererWrapper.wrapIfNecessary(new GotoApiSearchEverywhereContributor(initEvent, GotoApiModel.getInstance(initEvent.getProject())));
        }
    }
}
