package com.zys.http.extension.gutter;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.codeInsight.daemon.GutterName;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.zys.http.constant.SpringEnum;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.setting.HttpSetting;
import com.zys.http.ui.icon.HttpIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtClassBody;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import javax.swing.*;
import java.util.Arrays;
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
        if (element instanceof PsiMethod psiMethod) {
            return javaLineMarkInfo(psiMethod);
        }

        if (element instanceof KtNamedFunction function) {
            return kotlinLineMarkInfo(function);
        }

        return null;
    }

    private @Nullable HttpLineMarkerInfo javaLineMarkInfo(@NotNull PsiMethod psiMethod) {
        if (Objects.isNull(psiMethod.getNameIdentifier())) {
            return null;
        }
        PsiClass psiClass = (PsiClass) psiMethod.getParent();
        if (Objects.isNull(psiClass)) {
            return null;
        }
        String customAnno = HttpSetting.getInstance().getCustomAnno();

        boolean hasController = Arrays.stream(psiClass.getAnnotations()).anyMatch(a ->
                SpringEnum.Controller.CONTROLLER.getClazz().equals(a.getQualifiedName()) ||
                SpringEnum.Controller.REST_CONTROLLER.getClazz().equals(a.getQualifiedName()) ||
                (CharSequenceUtil.isNotBlank(customAnno) && customAnno.equals(a.getQualifiedName()))
        );
        if (!hasController) {
            return null;
        }

        return new HttpLineMarkerInfo(psiMethod.getNameIdentifier());
    }

    private @Nullable HttpLineMarkerInfo kotlinLineMarkInfo(@NotNull KtNamedFunction function) {
        if (Objects.isNull(function.getNameIdentifier())) {
            return null;
        }
        PsiElement parent = function.getParent();
        if (!(parent instanceof KtClassBody classBody)) {
            return null;
        }
        parent = classBody.getParent();
        if (!(parent instanceof KtClass ktClass)) {
            return null;
        }
        String customAnno = HttpSetting.getInstance().getCustomAnno();

        List<String> annotations = ktClass.getAnnotationEntries().stream().map(KtAnnotationEntry::getShortName).filter(Objects::nonNull).map(Name::asString).toList();

        boolean hasController = annotations.stream().anyMatch(o ->
                SpringEnum.Controller.CONTROLLER.getClazz().equals(o) || SpringEnum.Controller.CONTROLLER.getShortClassName().equals(o) ||
                SpringEnum.Controller.REST_CONTROLLER.getClazz().equals(o) || SpringEnum.Controller.REST_CONTROLLER.getShortClassName().equals(o) ||
                (CharSequenceUtil.isNotBlank(customAnno) && customAnno.equals(o) || customAnno.substring(customAnno.lastIndexOf('.') + 1).equals(o))
        );

        if (!hasController) {
            return null;
        }

        return new HttpLineMarkerInfo(function.getNameIdentifier());
    }


    @Override
    public @Nullable("null means disabled") @GutterName String getName() {
        return Bundle.get("http.extension.gutter");
    }

    @Override
    public @Nullable Icon getIcon() {
        return HttpIcons.HttpMethod.REQUEST;
    }
}
