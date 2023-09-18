package com.zys.http.tool;

import com.zys.http.ui.tree.node.BaseNode;
import com.zys.http.ui.tree.node.PackageNode;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @Description("获取指定父节点的所有包子节点")
    public static List<PackageNode> findChildren(@NotNull BaseNode<?> parentNode) {
        List<PackageNode> children = new ArrayList<>();
        Enumeration<TreeNode> enumeration = parentNode.children();
        while (enumeration.hasMoreElements()) {
            TreeNode ele = enumeration.nextElement();
            if (ele instanceof PackageNode p) {
                children.add(p);
            }
        }
        return children;
    }
}
