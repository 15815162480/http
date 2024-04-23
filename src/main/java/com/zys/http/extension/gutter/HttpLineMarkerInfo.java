package com.zys.http.extension.gutter;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
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

import java.util.Arrays;
import java.util.Objects;

/**
 * @author zhou ys
 * @since 2023-10-12
 */
public class HttpLineMarkerInfo extends LineMarkerInfo<PsiIdentifier> {
    private final PsiIdentifier element;

    public HttpLineMarkerInfo(@NotNull PsiIdentifier element) {
        super(element, element.getTextRange(), HttpIcons.HttpMethod.REQUEST, null,
                null, GutterIconRenderer.Alignment.LEFT, () -> "");
        this.element = element;
    }

    @Override
    public GutterIconRenderer createGutterRenderer() {
        PsiElement parent = element.getParent();
        if (!(parent instanceof PsiMethod psiMethod)) {
            return null;
        }
        if (Arrays.stream(SpringEnum.Method.values()).map(SpringEnum.Method::getClazz).noneMatch(psiMethod::hasAnnotation)) {
            return null;
        }

        // Alt Enter 的提示语
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
                messageBus.syncPublisher(TreeTopic.SELECTED_TOPIC).selected(psiMethod);
                contentManager.setSelectedContent(content);
            }
        });
        return new HttpGutterIconRenderer(this, commonAction);
    }
}
