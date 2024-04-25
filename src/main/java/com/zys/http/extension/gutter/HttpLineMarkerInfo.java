package com.zys.http.extension.gutter;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.messages.MessageBus;
import com.zys.http.action.CommonAction;
import com.zys.http.constant.HttpConstant;
import com.zys.http.constant.SpringEnum;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.topic.TreeTopic;
import com.zys.http.ui.icon.HttpIcons;
import com.zys.http.window.request.RequestWindow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author zhou ys
 * @since 2023-10-12
 */
public class HttpLineMarkerInfo extends LineMarkerInfo<PsiElement> {
    private final PsiElement element;

    public HttpLineMarkerInfo(@NotNull PsiElement element) {
        super(element, element.getTextRange(), HttpIcons.HttpMethod.REQUEST, null,
                null, GutterIconRenderer.Alignment.LEFT, () -> "");
        this.element = element;
    }

    @Override
    public GutterIconRenderer createGutterRenderer() {
        PsiElement parent = element.getParent();

        if (parent instanceof PsiMethod psiMethod) {
            if (Arrays.stream(SpringEnum.Method.values()).map(SpringEnum.Method::getClazz).noneMatch(psiMethod::hasAnnotation)) {
                return null;
            }
            return new HttpGutterIconRenderer(this, createAction(psiMethod));
        }

        if (parent instanceof KtNamedFunction function) {
            List<String> annotations = function.getAnnotationEntries().stream().map(KtAnnotationEntry::getShortName).filter(Objects::nonNull).map(Name::asString).toList();
            if (annotations.stream().noneMatch(o -> SpringEnum.Method.contains(o) || SpringEnum.Method.contains("org.springframework.web.bind.annotation." + o))) {
                return null;
            }
            return new HttpGutterIconRenderer(this, createAction(function));
        }

        return null;
    }

    private @NotNull CommonAction createAction(PsiElement psiElement) {
        CommonAction commonAction = new CommonAction(Bundle.get("http.extension.gutter"), "Test", myIcon);
        commonAction.setAction(event -> {
            ToolWindowManager manager = ToolWindowManager.getInstance(Objects.requireNonNull(event.getProject()));
            ToolWindow toolWindow = manager.getToolWindow(HttpConstant.PLUGIN_NAME);
            if (Objects.isNull(toolWindow)) {
                return;
            }
            toolWindow.show();
            ContentManager contentManager = toolWindow.getContentManager();
            Content content = contentManager.getContent(0);

            if (Objects.nonNull(content) && content.getComponent() instanceof RequestWindow) {
                MessageBus messageBus = event.getProject().getMessageBus();
                messageBus.syncPublisher(TreeTopic.REFRESH_TOPIC).refresh(false);
                messageBus.syncPublisher(TreeTopic.SELECTED_TOPIC).selected((NavigatablePsiElement) psiElement);
                contentManager.setSelectedContent(content);
            }
        });
        return commonAction;
    }
}
