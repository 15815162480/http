package com.zys.http.entity.tree;

import com.intellij.icons.AllIcons;
import jdk.jfr.Description;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
@Getter
@Description("模块结点数据")
@EqualsAndHashCode(callSuper = true)
public class ModuleNodeData extends NodeData {
    private final String contextPath;

    public ModuleNodeData(String nodeName, String contextPath) {
        super(nodeName);
        this.setNodeIcon(AllIcons.Nodes.Module);
        this.contextPath = contextPath;
    }
}
