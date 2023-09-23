package com.zys.http.entity.tree;

import com.zys.http.tool.ui.ThemeTool;
import com.zys.http.ui.icon.HttpIcons;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ModuleNodeData extends NodeData {

    private final String contextPath;

    public ModuleNodeData(String nodeName, String contextPath) {
        super(nodeName);
        this.setNodeIcon(ThemeTool.isDark() ? HttpIcons.TreeNode.MODULE : HttpIcons.TreeNode.MODULE_LIGHT);
        this.contextPath = contextPath;
    }
}
