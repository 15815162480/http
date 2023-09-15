package com.zys.http.entity.tree;

import com.zys.http.ui.icon.HttpIcons;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
public class PackageNodeData extends NodeData {

    public PackageNodeData(String nodeName) {
        super(nodeName);
        this.setNodeIcon(HttpIcons.TreeNode.PACKAGE);
    }
}
