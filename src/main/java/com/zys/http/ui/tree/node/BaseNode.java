package com.zys.http.ui.tree.node;

import com.jetbrains.rd.util.reactive.ISource;
import jdk.jfr.Description;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * @author zhou ys
 * @since 2023-09-05
 */
@Data
@Description("树形结构数据的结点")
@EqualsAndHashCode(callSuper = true)
public abstract class BaseNode<T> extends DefaultMutableTreeNode implements ISource<T> {

    @NotNull
    private transient T value;

    protected BaseNode(@NotNull T value) {
        super(value);
        this.value = value;
    }

    @Nullable
    public Icon getIcon(boolean selected) {
        return null;
    }

    @Override
    public void add(@NotNull MutableTreeNode newChild) {
        if (newChild instanceof BaseNode<?>) {
            addNode(((BaseNode<?>) newChild));
        }
    }

    private void addNode(@NotNull BaseNode<?> newChild) {
        super.add(newChild);
    }
}
