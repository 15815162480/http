package com.zys.http.ui.search.where;

import com.intellij.ide.actions.SearchEverywherePsiRenderer;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.util.Processor;
import com.zys.http.ui.search.ApiSearchItem;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.lang.ref.WeakReference;

/**
 * @author zys
 * @since 2023-10-10
 */
public class ApiSearchEverywhereContributor implements SearchEverywhereContributor<ApiSearchItem> {

    private final Project project;
    private final WeakReference<Component> contextComponnet;
    private final GotoApiModel apiModel;
    private final GotoApiItemProvider provider;


    public ApiSearchEverywhereContributor(Project project, Component contextComponent, Editor editor) {
        this.project = project;
        this.contextComponnet = new WeakReference<>(contextComponent);
        this.apiModel = new GotoApiModel(project, contextComponent, editor);
        this.provider = new GotoApiItemProvider(apiModel);
    }

    @Override
    public @NotNull String getSearchProviderId() {
        return ApiSearchEverywhereContributor.class.getName();
    }

    @Override
    public @NotNull @Nls String getGroupName() {
        return "ApiTool";
    }

    @Override
    public int getSortWeight() {
        return 0;
    }

    @Override
    public boolean showInFindResults() {
        return true;
    }

    @Override
    public void fetchElements(@NotNull String pattern, @NotNull ProgressIndicator progressIndicator, @NotNull Processor<? super ApiSearchItem> consumer) {

    }

    @Override
    public boolean processSelectedItem(@NotNull ApiSearchItem selected, int modifiers, @NotNull String searchText) {
        return false;
    }

    @Override
    public @NotNull ListCellRenderer<? super ApiSearchItem> getElementsRenderer() {
        return new SearchEverywherePsiRenderer(this);
    }

    @Override
    public @Nullable Object getDataForItem(@NotNull ApiSearchItem element, @NotNull String dataId) {
        // 获取选定元素的上下文数据。
        // 形参:
        // 元素-dataId 选定项- DataKey ID
        // 请参阅:
        // DataKey, DataContext
        return element.getMethodNodeData().getNodeName();
    }

    public static class Factory implements SearchEverywhereContributorFactory<ApiSearchItem> {

        @Override
        public @NotNull SearchEverywhereContributor<ApiSearchItem> createContributor(@NotNull AnActionEvent initEvent) {
            return new ApiSearchEverywhereContributor(initEvent.getProject(),
                    initEvent.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT),
                    initEvent.getData(CommonDataKeys.EDITOR));
        }
    }
}
