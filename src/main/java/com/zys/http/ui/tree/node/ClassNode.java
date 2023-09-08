package com.zys.http.ui.tree.node;

import com.zys.http.entity.tree.ClassNodeData;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
@Description("类结点")
public class ClassNode extends BaseNode<ClassNodeData> {
    public ClassNode(@NotNull ClassNodeData value) {
        super(value);
    }
}
