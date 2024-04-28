package com.zys.http.extension.intention;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.SpringEnum;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.service.NotifyService;
import com.zys.http.tool.KotlinTool;
import com.zys.http.tool.ProjectTool;
import com.zys.http.tool.SystemTool;
import com.zys.http.tool.UrlTool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import java.util.List;
import java.util.Objects;

/**
 * @author zhou ys
 * @since 2024-04-28
 */
public class KotlinCopyApiIntentionAction extends PsiElementBaseIntentionAction {
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        KtNamedFunction function = PsiTreeUtil.getParentOfType(psiElement, KtNamedFunction.class);
        if (Objects.isNull(function)) {
            return;
        }
        List<String> annotations = function.getAnnotationEntries().stream().map(KtAnnotationEntry::getShortName).filter(Objects::nonNull).map(Name::asString).toList();
        if (annotations.isEmpty()) {
            return;
        }
        String apiPath = apiPath(project, function);
        SystemTool.copy2Clipboard(apiPath);
        NotifyService.instance(project).info(Bundle.get("http.api.tree.method.right.menu.action.copy.api.msg"));
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        KtNamedFunction function = PsiTreeUtil.getParentOfType(psiElement, KtNamedFunction.class);
        if (Objects.isNull(function)) {
            return false;
        }
        List<String> annotations = function.getAnnotationEntries().stream().map(KtAnnotationEntry::getShortName).filter(Objects::nonNull).map(Name::asString).toList();
        if (annotations.isEmpty()) {
            return false;
        }

        return annotations.stream().anyMatch(o -> SpringEnum.Method.contains(o) || SpringEnum.Method.contains("org.springframework.web.bind.annotation." + o));
    }

    @Override
    public @NotNull @IntentionName String getText() {
        return Bundle.get("http.api.tree.method.right.menu.action.copy.api");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return getText();
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    static @Nullable String apiPath(Project project, @NotNull KtNamedFunction function) {
        PsiElement parent = function.getParent();
        if (Objects.isNull(parent)) {
            return null;
        }
        parent = parent.getParent();
        if (Objects.isNull(parent)) {
            return null;
        }

        Module module = ModuleUtilCore.findModuleForPsiElement(parent);
        if (Objects.isNull(module)) {
            return null;
        }
        String contextPath = ProjectTool.getModuleContextPath(project, module);
        String controllerPath = KotlinTool.Class.getKtControllerPath((KtClass) parent);
        List<KtAnnotationEntry> entries = function.getAnnotationEntries();
        String methodPath = "";
        for (KtAnnotationEntry o : entries) {
            HttpEnum.HttpMethod httpMethod = SpringEnum.Method.get(Objects.requireNonNull(o.getShortName()).asString());
            if (Objects.isNull(httpMethod)) {
                httpMethod = SpringEnum.Method.get("org.springframework.web.bind.annotation." + Objects.requireNonNull(o.getShortName()).asString());
                if (HttpEnum.HttpMethod.REQUEST.equals(httpMethod)) {
                    httpMethod = HttpEnum.HttpMethod.requestMappingConvert(o);
                }
            }
            if (Objects.nonNull(httpMethod)) {
                methodPath = KotlinTool.Annotation.getAnnotationValue(o, new String[]{"value", "path"});
                break;
            }
        }

        return UrlTool.buildMethodUri(contextPath, controllerPath, methodPath);
    }
}
