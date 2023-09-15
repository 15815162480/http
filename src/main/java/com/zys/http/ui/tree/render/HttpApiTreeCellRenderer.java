package com.zys.http.ui.tree.render;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.zys.http.entity.tree.NodeData;
import com.zys.http.ui.tree.node.BaseNode;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author zys
 * @since 2023-09-08
 */
@Description("树形结点展示内容自定义渲染器")
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
        if (value instanceof BaseNode<? extends NodeData> node) {
            setIcon(node.getIcon());
            append(node.getFragment(), node.getTextAttributes());
            String description = node.getValue().getDescription();
            if (CharSequenceUtil.isNotEmpty(description)) {
                setToolTipText(description);
            }
        }
    }
}
