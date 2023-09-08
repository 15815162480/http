package com.zys.http.ui.tree.node;

import com.zys.http.entity.tree.ProjectNodeData;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
@Description("项目结点, 一般为根结点")
public class ProjectNode extends BaseNode<ProjectNodeData> {
    public ProjectNode(@NotNull ProjectNodeData value) {
        super(value);
    }
}
