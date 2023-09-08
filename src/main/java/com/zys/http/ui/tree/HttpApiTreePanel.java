package com.zys.http.ui.tree;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.treeStructure.SimpleTree;
import com.zys.http.constant.HttpEnum;
import com.zys.http.entity.tree.*;
import com.zys.http.ui.tree.node.*;

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

        ProjectNodeData data = new ProjectNodeData();
        data.setNodeName("Project");
        ProjectNode root = new ProjectNode(data);

        ModuleNodeData nodeData = new ModuleNodeData();
        nodeData.setNodeName("Module");
        ModuleNode moduleNode = new ModuleNode(nodeData);

        PackageNodeData packageNodeData = new PackageNodeData();
        packageNodeData.setNodeName("Package");
        PackageNode packageNode = new PackageNode(packageNodeData);

        ClassNodeData classNodeData = new ClassNodeData();
        classNodeData.setNodeName("Class");
        ClassNode classNode = new ClassNode(classNodeData);

        for (HttpEnum.HttpMethod value : HttpEnum.HttpMethod.values()) {
            MethodNodeData methodNodeData = new MethodNodeData(value);
            methodNodeData.setNodeName(value.name());
            MethodNode methodNode = new MethodNode(methodNodeData);
            classNode.add(methodNode);
        }

        packageNode.add(classNode);
        moduleNode.add(packageNode);
        root.add(moduleNode);
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
