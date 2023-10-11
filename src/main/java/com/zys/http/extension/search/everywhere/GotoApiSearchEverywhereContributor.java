package com.zys.http.extension.search.everywhere;

import com.intellij.ide.actions.searcheverywhere.AbstractGotoSEContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.zys.http.constant.HttpConstant;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author zys
 * @since 2023-10-10
 */
public class GotoApiSearchEverywhereContributor extends AbstractGotoSEContributor {
    private final GotoApiModel apiModel;

    public GotoApiSearchEverywhereContributor(@NotNull AnActionEvent event) {
        super(event);
        this.apiModel = GotoApiModel.getInstance(event.getProject());
    }

    @Override
    public boolean isShownInSeparateTab() {
        return !apiModel.getNodeDataList().isEmpty();
    }

    @Override
    public @NotNull String getSearchProviderId() {
        return GotoApiSearchEverywhereContributor.class.getName();
    }

    @Override
    public @NotNull @Nls String getGroupName() {
        return HttpConstant.PLUGIN_NAME;
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
            return new GotoApiSearchEverywhereContributor(initEvent);
        }
    }
}
