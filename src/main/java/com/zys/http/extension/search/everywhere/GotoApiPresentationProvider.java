package com.zys.http.extension.search.everywhere;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProvider;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.zys.http.entity.tree.MethodNodeData;
import com.zys.http.ui.icon.HttpIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Optional;

/**
 * @author zhou ys
 * @since 2023-12-21
 */
public class GotoApiPresentationProvider implements ItemPresentationProvider<GotoApiItem> {
    @Override
    public @NotNull ItemPresentation getPresentation(@NotNull GotoApiItem gotoApiItem) {
        MethodNodeData methodNodeData = gotoApiItem.methodNodeData();
        NavigatablePsiElement psiElement = methodNodeData.getPsiElement();
        PsiMethod psiMethod = ((PsiMethod) psiElement);
        PsiClass psiClass = ApplicationManager.getApplication().runReadAction((Computable<PsiClass>) () -> (PsiClass) psiMethod.getParent());
        String location = Optional.ofNullable(psiClass).map(NavigationItem::getName).orElse("") + "#" + psiMethod.getName();
        Icon icon = HttpIcons.HttpMethod.getHttpMethodIcon(methodNodeData.getHttpMethod());
        return new PresentationData(gotoApiItem.getName(), location, icon, null);
    }
}
