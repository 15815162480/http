package com.zys.http.tool.ui;

import com.zys.http.ui.tree.node.BaseNode;
import com.zys.http.ui.tree.node.MethodNode;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author zhou ys
 * @since 2023-09-18
 */
@Description("树形节点工具类")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TreeTool {
    @Nullable
    @Description("获取指定父节点的指定子节点")
    public static BaseNode<?> findChild(@NotNull BaseNode<?> parentNode, @NotNull String childName) {
        Enumeration<TreeNode> children = parentNode.children();
        while (children.hasMoreElements()) {
            TreeNode child = children.nextElement();
            if (!(child instanceof BaseNode<?> childNode)) {
                continue;
            }
            if (childName.equals(childNode.getValue().getNodeName())) {
                return childNode;
            }
        }
        return null;
    }

    @NotNull
    @Description("获取指定父节点的所有子节点")
    public static List<BaseNode<?>> findChildren(@NotNull BaseNode<?> parentNode) {
        List<BaseNode<?>> children = new ArrayList<>();
        Enumeration<TreeNode> enumeration = parentNode.children();
        while (enumeration.hasMoreElements()) {
            TreeNode ele = enumeration.nextElement();
            if (ele instanceof BaseNode<?> p) {
                children.add(p);
            }
        }
        return children;
    }

    public static void removeEmptyParentNodes(JTree tree) {
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        removeEmptyParentNodes(root, treeModel);
    }

    private static void removeEmptyParentNodes(DefaultMutableTreeNode node, DefaultTreeModel treeModel) {
        Enumeration<TreeNode> children = node.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            removeEmptyParentNodes(child, treeModel);
        }

        if (node.isLeaf() && node.getParent() != null && !(node instanceof MethodNode)) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
            treeModel.removeNodeFromParent(node);
            removeEmptyParentNodes(parent, treeModel);
        }
    }
}
