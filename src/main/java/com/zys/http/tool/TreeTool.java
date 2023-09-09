package com.zys.http.tool;

import com.intellij.openapi.project.Project;
import com.zys.http.entity.tree.ModuleNodeData;
import com.zys.http.entity.tree.NodeData;
import com.zys.http.entity.tree.ProjectNodeData;
import com.zys.http.ui.tree.node.ModuleNode;
import com.zys.http.ui.tree.node.ProjectNode;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * @author zys
 * @since 2023-09-09
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TreeTool {

    @Description("构建树形根结点")
    public static ProjectNode createTreeRootNode(@NotNull Project project) {
        ProjectNodeData data = ProjectTool.buildHttpApiTreeNodeData(project);
        ProjectNode node = new ProjectNode(data);
        List<? extends NodeData> children = data.getChildren();
        if (Objects.isNull(children)) {
            return node;
        }
        for (NodeData child : children) {
            ModuleNode moduleNode = new ModuleNode((ModuleNodeData) child);
            addChildNodes(moduleNode, child.getChildren());
            node.add(moduleNode);
        }
        return node;
    }

    private static void addChildNodes(ModuleNode moduleNode, List<? extends NodeData> children) {
        if (Objects.isNull(children) || children.isEmpty()) {
            return;
        }
        for (NodeData child : children) {
            ModuleNode childNode = new ModuleNode((ModuleNodeData) child);
            addChildNodes(childNode, child.getChildren());
            moduleNode.add(childNode);
        }
    }
}
