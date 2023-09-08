package com.zys.http.ui.tree.node;

import com.zys.http.entity.tree.ClassNodeData;
import org.jetbrains.annotations.NotNull;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
public class ClassNode extends BaseNode<ClassNodeData> {
    protected ClassNode(@NotNull ClassNodeData value) {
        super(value);
    }
}
