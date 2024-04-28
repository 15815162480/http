package com.zys.http.window.request.panel;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.zys.http.constant.HttpEnum;
import com.zys.http.extension.topic.TreeTopic;
import com.zys.http.ui.tree.node.BaseNode;
import com.zys.http.ui.tree.node.MethodNode;

import java.awt.*;
import java.util.List;

/**
 * @author zhou ys
 * @since 2024-04-16
 */
public class RequestPanel extends JBSplitter {
    private final transient Project project;
    private final ApiTreePanel apiTreePanel;
    private final ConfigPanel configPanel;

    public RequestPanel(Project project) {
        super(true, Window.class.getName(), 0.5F);
        this.project = project;
        this.apiTreePanel = new ApiTreePanel(project);
        this.configPanel = new ConfigPanel(project);
        this.apiTreePanel.setChooseCallback(configPanel::chooseEvent);
        this.setFirstComponent(apiTreePanel);
        this.setSecondComponent(configPanel);
        initTopic();
    }

    private void initTopic() {
        this.project.getMessageBus().connect().subscribe(TreeTopic.SELECTED_TOPIC, (TreeTopic.Selected) psiMethod -> {
            MethodNode methodNode = this.apiTreePanel.getMethodNode(psiMethod);
            this.apiTreePanel.setSelectedNode(methodNode);
            this.configPanel.reload(methodNode);
        });
    }

    public void loadNodes(List<HttpEnum.HttpMethod> methods, List<String> nodeShowValues, List<HttpEnum.Language> languageValues) {
        this.apiTreePanel.loadNodes(methods, nodeShowValues, languageValues);
    }

    public void treeExpand() {
        this.apiTreePanel.treeExpand();
    }

    public void treeCollapse() {
        this.apiTreePanel.treeCollapse();
    }

    public void clearApiTree() {
        this.apiTreePanel.clear();
    }

    public void reload(BaseNode<?> chooseNode) {
        this.configPanel.reload(chooseNode);
    }

    public BaseNode<?> getApiTreeChooseNode() {
        return this.apiTreePanel.getChooseNode();
    }

    public void treeExpandAll() {
        this.apiTreePanel.expandAll();
    }
}
