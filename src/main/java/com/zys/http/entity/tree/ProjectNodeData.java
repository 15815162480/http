package com.zys.http.entity.tree;

import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
@Description("树形列表项目结点")
public class ProjectNodeData extends NodeData {
    public ProjectNodeData() {
        this.setNodeIcon(HttpIcons.MODULE);
    }
}
