package com.zys.http.extension.search.everywhere;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiMethod;
import com.zys.http.entity.tree.MethodNodeData;
import com.zys.http.tool.ThreadTool;
import com.zys.http.ui.icon.HttpIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
    public @NotNull ItemPresentation getPresentation() {
        AtomicReference<String> location = new AtomicReference<>("");
        NavigatablePsiElement psiElement = methodNodeData.getPsiElement();
        PsiMethod psiMethod = ((PsiMethod) psiElement);
        ReadAction.nonBlocking(psiMethod::getContainingClass).finishOnUiThread(ModalityState.defaultModalityState(),
                psiClass -> location.set(Optional.ofNullable(psiClass).map(NavigationItem::getName).orElse("") + "#" + psiMethod.getName())
        ).submit(ThreadTool.getExecutor());
        Icon icon = HttpIcons.HttpMethod.getHttpMethodIcon(methodNodeData.getHttpMethod());
        return new PresentationData(getName(), location.get(), icon, null);
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
