package com.zys.http.ui.tree;

import com.intellij.ide.TreeExpander;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.zys.http.entity.tree.NodeData;
import com.zys.http.ui.tree.node.BaseNode;
import com.zys.http.ui.tree.render.HttpApiTreeCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.function.Consumer;

import static com.zys.http.constant.UIConstant.BORDER_COLOR;

/**
 * @author zhou ys
 * @since 2023-09-05
 */
public abstract class AbstractListTreePanel extends JBScrollPane implements TreeExpander {
    protected final JTree tree;

    protected AbstractListTreePanel(@NotNull final JTree tree) {
        this.tree = tree;
        this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.setBorder(JBUI.Borders.customLineTop(BORDER_COLOR));
        this.tree.setRootVisible(true);
        this.tree.setShowsRootHandles(true);
        this.tree.createToolTip();
        this.setViewportView(tree);
        this.tree.setCellRenderer(new HttpApiTreeCellRenderer());

        tree.addTreeSelectionListener(e -> {
            if (!tree.isEnabled()) {
                return;
            }
            Object component = tree.getLastSelectedPathComponent();
            if (!(component instanceof BaseNode<? extends NodeData> selectedNode)) {
                return;
            }
            Objects.requireNonNull(getChooseListener()).accept(selectedNode);
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (!tree.isEnabled()) {
                    return;
                }
                BaseNode<?> node = getNode(event);
                if (node == null) {
                    return;
                }
                if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2 && getDoubleClickListener() != null) {
                        getDoubleClickListener().accept(node);

                }
                // } else if (SwingUtilities.isRightMouseButton(event)) {
                //     showPopupMenu(event.getX(), event.getY(), getPopupMenu(event, node));
                // }
            }

            @Nullable
            private BaseNode<?> getNode(@NotNull MouseEvent event) {
                TreePath path = tree.getPathForLocation(event.getX(), event.getY());
                tree.setSelectionPath(path);
                return getChooseNode(path);
            }
        });
    }

    protected final DefaultTreeModel getTreeModel() {
        return (DefaultTreeModel) tree.getModel();
    }

    @Override
    public boolean canExpand() {
        return tree.getRowCount() > 0;
    }

    @Override
    public boolean canCollapse() {
        return tree.getRowCount() > 0;
    }

    @Override
    public void collapseAll() {

    }

    @Nullable
    protected abstract Consumer<BaseNode<?>> getChooseListener();

    @Nullable
    protected abstract Consumer<BaseNode<?>> getDoubleClickListener();

    @Nullable
    public BaseNode<?> getChooseNode(@Nullable TreePath treePath) {
        Object component;
        if (treePath != null) {
            component = treePath.getLastPathComponent();
        } else {
            component = tree.getLastSelectedPathComponent();
        }
        if (!(component instanceof BaseNode<?>)) {
            return null;
        }
        return (BaseNode<?>) component;
    }
}
