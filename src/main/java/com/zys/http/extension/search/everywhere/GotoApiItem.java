package com.zys.http.extension.search.everywhere;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.zys.http.entity.tree.MethodNodeData;
import com.zys.http.ui.icon.HttpIcons;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * @author zhou ys
 * @since 2023-10-10
 */
@Getter
@EqualsAndHashCode
public class GotoApiItem implements NavigationItem, Comparable<GotoApiItem> {

    private final MethodNodeData methodNodeData;

    public GotoApiItem(MethodNodeData methodNodeData) {
        this.methodNodeData = methodNodeData;
    }

    @Override
    public @Nullable String getName() {
        return methodNodeData.getNodeName();
    }

    @Override
    public @Nullable ItemPresentation getPresentation() {
        String location = "";
        NavigatablePsiElement psiElement = methodNodeData.getPsiElement();
        PsiMethod psiMethod = ((PsiMethod) psiElement);
        PsiClass psiClass = psiMethod.getContainingClass();
        if (psiClass != null) {
            location = psiClass.getName();
        }
        location += "#" + psiMethod.getName();
        Icon icon = HttpIcons.HttpMethod.getHttpMethodIcon(methodNodeData.getHttpMethod());
        return new PresentationData(getName(), location, icon, null);
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
