package com.zys.http.entity.tree;

import com.intellij.icons.AllIcons;
import jdk.jfr.Description;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
@Description("包结点数据")
public class PackageNodeData extends NodeData {
    public PackageNodeData(String nodeName) {
        super(nodeName);
        this.setNodeIcon(AllIcons.Nodes.Package);
    }
}
