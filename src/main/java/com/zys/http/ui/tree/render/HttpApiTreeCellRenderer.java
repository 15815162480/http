package com.zys.http.ui.tree.render;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.zys.http.entity.tree.NodeData;
import com.zys.http.ui.tree.node.BaseNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author zys
 * @since 2023-09-08
 */
public class HttpApiTreeCellRenderer extends ColoredTreeCellRenderer {
    @Override
    public void customizeCellRenderer(
            @NotNull JTree tree,
            Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus
    ) {
        BaseNode<? extends NodeData> node = null;
        if (value instanceof BaseNode) {
            node = (BaseNode<? extends NodeData>) value;
        }
        if (node != null) {
            setIcon(node.getIcon(selected));
            append(node.getFragment(), node.getTextAttributes());
        }
    }
}
