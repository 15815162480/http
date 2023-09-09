package com.zys.http.ui.tree;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.treeStructure.SimpleTree;
import com.zys.http.tool.TreeTool;
import com.zys.http.ui.tree.node.MethodNode;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zys
 * @since 2023-09-08
 */
public class HttpApiTreePanel extends AbstractListTreePanel {

    private final transient Project project;

    private final transient Map<PsiMethod, MethodNode> methodNodes = new HashMap<>();

    public HttpApiTreePanel(@NotNull Project project) {
        super(new SimpleTree());
        this.project = project;
        super.getTreeModel().setRoot(TreeTool.createTreeRootNode(project));
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
