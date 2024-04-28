package com.zys.http.extension.intention;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.zys.http.constant.SpringEnum;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.service.NotifyService;
import com.zys.http.tool.JavaTool;
import com.zys.http.tool.ProjectTool;
import com.zys.http.tool.SystemTool;
import com.zys.http.tool.UrlTool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author zhou ys
 * @since 2024-04-24
 */
public class JavaCopyApiIntentionAction extends PsiElementBaseIntentionAction {
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        PsiMethod psiMethod = PsiTreeUtil.getParentOfType(psiElement, PsiMethod.class);

        if (Objects.isNull(psiMethod)) {
            return;
        }
        String apiPath = apiPath(project, psiMethod);
        if (Objects.isNull(apiPath)) {
            return;
        }
        SystemTool.copy2Clipboard(apiPath);

        NotifyService.instance(project).info(Bundle.get("http.api.tree.method.right.menu.action.copy.api.msg"));
    }

    @Override
    public @NotNull @IntentionName String getText() {
        return Bundle.get("http.api.tree.method.right.menu.action.copy.api");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        PsiMethod psiMethod = PsiTreeUtil.getParentOfType(psiElement, PsiMethod.class);
        if (Objects.isNull(psiMethod)) {
            return false;
        }

        PsiAnnotation[] annotations = psiMethod.getAnnotations();
        if (annotations.length == 0) {
            return false;
        }
        return Stream.of(annotations).anyMatch(SpringEnum.Method::contains);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return getText();
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    static @Nullable String apiPath(Project project, @NotNull PsiMethod psiMethod) {
        PsiClass containingClass = psiMethod.getContainingClass();
        if (Objects.isNull(containingClass)) {
            return null;
        }

        Module module = ModuleUtilCore.findModuleForPsiElement(containingClass);
        if (Objects.isNull(module)) {
            return null;
        }
        String contextPath = ProjectTool.getModuleContextPath(project, module);
        String controllerPath = JavaTool.Class.getControllerPath(containingClass);
        PsiAnnotation[] annotations = psiMethod.getAnnotations();
        if (annotations.length == 0) {
            return null;
        }
        String methodPath = Stream.of(annotations).filter(SpringEnum.Method::contains)
                .map(annotation -> JavaTool.Annotation.getAnnotationValue(annotation, new String[]{"value", "path"}))
                .findFirst().orElse(null);

        return UrlTool.buildMethodUri(contextPath, controllerPath, methodPath);
    }
}
