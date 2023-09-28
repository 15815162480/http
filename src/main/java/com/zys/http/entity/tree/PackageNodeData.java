package com.zys.http.entity.tree;

import com.zys.http.tool.ui.ThemeTool;
import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
@Description("包结点数据")
public class PackageNodeData extends NodeData {

    public PackageNodeData(String nodeName) {
        super(nodeName);
        this.setNodeIcon(ThemeTool.isDark() ? HttpIcons.TreeNode.PACKAGE : HttpIcons.TreeNode.PACKAGE_LIGHT);
    }
}
