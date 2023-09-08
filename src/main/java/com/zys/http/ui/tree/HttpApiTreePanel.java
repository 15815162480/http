package com.zys.http.ui.tree;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.treeStructure.SimpleTree;
import com.zys.http.entity.tree.ModuleNodeData;
import com.zys.http.entity.tree.ProjectNodeData;
import com.zys.http.ui.tree.node.MethodNode;
import com.zys.http.ui.tree.node.ModuleNode;
import com.zys.http.ui.tree.node.ProjectNode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zys
 * @since 2023-09-08
 */
public class HttpApiTreePanel extends AbstractListTreePanel {

    private final transient Project project;

    private final transient Map<PsiMethod, MethodNode> methodNodes = new HashMap<>();

    public HttpApiTreePanel(Project project) {
        super(new SimpleTree());
        this.project = project;
        methodNodes.clear();
        ProjectNodeData data = new ProjectNodeData();
        data.setNodeName("Project");
        ProjectNode root = new ProjectNode(data);
        ModuleNodeData nodeData = new ModuleNodeData();
        nodeData.setNodeName("Module");
        root.add(new ModuleNode(nodeData));
        super.getTreeModel().setRoot(root);
    }

    @Override
    public boolean canExpand() {
        return true;
    }

    @Override
    public void collapseAll() {

    }

    @Override
    public boolean canCollapse() {
        return true;
    }
}
