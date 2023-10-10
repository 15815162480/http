package com.zys.http.ui.search;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.zys.http.entity.tree.MethodNodeData;
import com.zys.http.ui.icon.HttpIcons;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author zhou ys
 * @since 2023-10-10
 */
@Getter
public class ApiSearchItem implements NavigationItem {

    private final MethodNodeData methodNodeData;

    public ApiSearchItem(MethodNodeData methodNodeData) {
        this.methodNodeData = methodNodeData;
    }

    @Override
    public @Nullable String getName() {
        return methodNodeData.getNodeName();
    }

    @Override
    public @Nullable ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Override
            public @Nullable String getPresentableText() {
                return getName();
            }

            @Override
            public @NotNull String getLocationString() {
                String location = "";
                NavigatablePsiElement psiElement = methodNodeData.getPsiElement();
                PsiMethod psiMethod = ((PsiMethod) psiElement);
                PsiClass psiClass = psiMethod.getContainingClass();
                if (psiClass != null) {
                    location = psiClass.getName();
                }
                location += "#" + psiMethod.getName();
                return location;
            }

            @Override
            public @Nullable Icon getIcon(boolean unused) {
                return HttpIcons.HttpMethod.getHttpMethodIcon(methodNodeData.getHttpMethod());
            }
        };
    }

    @Override
    public void navigate(boolean requestFocus) {
        NavigatablePsiElement psiElement = methodNodeData.getPsiElement();
        if (psiElement != null) {
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
}
