package com.zys.http.extension.gutter;

import com.intellij.codeInsight.daemon.GutterName;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.zys.http.constant.SpringEnum;
import com.zys.http.extension.service.Bundle;
import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author zhou ys
 * @since 2023-10-12
 */
public class HttpLineMarkerProvider extends LineMarkerProviderDescriptor {
    private final Option httpGenerate = new Option("http.generate", "ApiTool", HttpIcons.HttpMethod.REQUEST);

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!httpGenerate.isEnabled()) {
            return null;
        }
        if (!(element instanceof PsiMethod psiMethod)) {
            return null;
        }

        if (Objects.isNull(psiMethod.getNameIdentifier())) {
            return null;
        }

        PsiClass psiClass = (PsiClass) psiMethod.getParent();
        if (Objects.isNull(psiClass)) {
            return null;
        }

        boolean hasController = Arrays.stream(psiClass.getAnnotations()).anyMatch(a ->
                SpringEnum.Controller.CONTROLLER.getClazz().equals(a.getQualifiedName()) ||
                SpringEnum.Controller.REST_CONTROLLER.getClazz().equals(a.getQualifiedName())
        );
        if (!hasController) {
            return null;
        }

        return new HttpLineMarkerInfo(psiMethod.getNameIdentifier());
    }

    @Override
    @Description("收集所有符合要求的")
    public void collectSlowLineMarkers(
            @NotNull List<? extends PsiElement> elements,
            @NotNull Collection<? super LineMarkerInfo<?>> result
    ) {
        for (PsiElement element : elements) {
            if (!(element instanceof PsiMethod psiMethod)) {
                return;
            }
            for (SpringEnum.Method value : SpringEnum.Method.values()) {
                if (psiMethod.hasAnnotation(value.getClazz())) {
                    result.add(getLineMarkerInfo(Objects.requireNonNull(psiMethod)));
                }
            }
        }
    }

    @Override
    public @Nullable("null means disabled") @GutterName String getName() {
        return Bundle.get("http.gutter.config");
    }

    @Override
    public @Nullable Icon getIcon() {
        return HttpIcons.HttpMethod.REQUEST;
    }
}
