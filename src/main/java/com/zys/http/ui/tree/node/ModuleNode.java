package com.zys.http.ui.tree.node;

import com.zys.http.entity.tree.ModuleNodeData;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
@Description("模块结点")
public class ModuleNode extends BaseNode<ModuleNodeData> {
    public ModuleNode(@NotNull ModuleNodeData value) {
        super(value);
    }
}
