package com.zys.http.ui.tree.node;

import com.zys.http.entity.tree.PackageNodeData;
import org.jetbrains.annotations.NotNull;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
public class PackageNode extends BaseNode<PackageNodeData> {
    public PackageNode(@NotNull PackageNodeData value) {
        super(value);
    }
}
