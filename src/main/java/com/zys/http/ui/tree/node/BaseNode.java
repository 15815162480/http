package com.zys.http.ui.tree.node;

import com.intellij.ui.SimpleTextAttributes;
import com.zys.http.entity.tree.NodeData;
import jdk.jfr.Description;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * @author zhou ys
 * @since 2023-09-05
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public abstract class BaseNode<T extends NodeData> extends DefaultMutableTreeNode {
    @NotNull
    private final transient T value;

    protected BaseNode(@NotNull T value) {
        super(value);
        this.value = value;
    }

    @Nullable
    public Icon getIcon() {
        return value.getNodeIcon();
    }

    @Override
    public void add(@NotNull MutableTreeNode newChild) {
        if (newChild instanceof BaseNode<? extends NodeData>) {
            addNode(((BaseNode<? extends NodeData>) newChild));
        }
    }

    @Description("添加子节点")
    private void addNode(@NotNull BaseNode<? extends NodeData> newChild) {
        super.add(newChild);
    }

    public @NotNull String getFragment() {
        return value.getNodeName();
    }

    public @NotNull SimpleTextAttributes getTextAttributes() {
        return SimpleTextAttributes.REGULAR_ATTRIBUTES;
    }
}
