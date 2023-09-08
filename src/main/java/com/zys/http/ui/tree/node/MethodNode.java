package com.zys.http.ui.tree.node;

import com.zys.http.entity.tree.MethodNodeData;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
@Description("方法结点")
public class MethodNode extends BaseNode<MethodNodeData> {
    public MethodNode(@NotNull MethodNodeData value) {
        super(value);
    }
}
