package com.zys.http.extension.gutter;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.zys.http.action.CommonAction;
import com.zys.http.constant.HttpConstant;
import com.zys.http.constant.SpringEnum;
import com.zys.http.extension.topic.RefreshTreeTopic;
import com.zys.http.ui.icon.HttpIcons;
import com.zys.http.ui.tree.HttpApiTreePanel;
import com.zys.http.ui.tree.node.MethodNode;
import com.zys.http.ui.window.RequestTabWindow;
import com.zys.http.ui.window.panel.RequestPanel;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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

        CommonAction commonAction = new CommonAction("Test", "Test", myIcon);
        commonAction.setAction(event -> {
            ToolWindowManager manager = ToolWindowManager.getInstance(Objects.requireNonNull(event.getProject()));
            ToolWindow toolWindow = manager.getToolWindow(HttpConstant.PLUGIN_NAME);
            if (Objects.nonNull(toolWindow)) {
                toolWindow.show();
                ContentManager contentManager = toolWindow.getContentManager();
                Content content = contentManager.getContent(0);
                if (Objects.nonNull(content) && content.getComponent() instanceof RequestTabWindow requestTabWindow) {
                    event.getProject().getMessageBus().syncPublisher(RefreshTreeTopic.TOPIC).refresh(false);
                    RequestPanel requestPanel = requestTabWindow.getRequestPanel();
                    HttpApiTreePanel httpApiTreePanel = requestPanel.getHttpApiTreePanel();
                    Map<PsiClass, List<MethodNode>> methodNodeMap = httpApiTreePanel.getMethodNodeMap();
                    PsiClass containingClass = psiMethod.getContainingClass();
                    MethodNode methodNode = methodNodeMap.getOrDefault(containingClass, new ArrayList<>()).stream()
                            .filter(v -> v.getValue().getPsiElement().equals(psiMethod)).findFirst().orElse(null);
                    httpApiTreePanel.setSelectedNode(methodNode);
                    requestPanel.reload(methodNode);
                }
            }
        });
        return new HttpGutterIconRenderer(this, commonAction);
    }
}
