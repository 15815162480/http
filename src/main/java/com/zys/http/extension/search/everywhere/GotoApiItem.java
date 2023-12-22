package com.zys.http.extension.search.everywhere;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiMethod;
import com.zys.http.entity.tree.MethodNodeData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author zhou ys
 * @since 2023-10-10
 */
public record GotoApiItem(MethodNodeData methodNodeData) implements NavigationItem, Comparable<GotoApiItem> {

    @Override
    public @Nullable String getName() {
        return methodNodeData.getNodeName();
    }

    @Override
    public @Nullable ItemPresentation getPresentation() {
        return ItemPresentationProviders.getItemPresentation(this);
    }

    @Override
    public void navigate(boolean requestFocus) {
        NavigatablePsiElement psiElement = methodNodeData.getPsiElement();
        if (psiElement instanceof PsiMethod) {
            psiElement.navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return methodNodeData.getPsiElement().canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    @Override
    public int compareTo(@NotNull GotoApiItem o) {
        return Objects.requireNonNull(getName()).compareTo(Objects.requireNonNull(o.getName()));
    }
}
