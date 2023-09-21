package com.zys.http.ui.tree;

import com.intellij.ide.TreeExpander;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.zys.http.entity.tree.NodeData;
import com.zys.http.ui.tree.node.BaseNode;
import com.zys.http.ui.tree.node.MethodNode;
import com.zys.http.ui.tree.node.ModuleNode;
import com.zys.http.ui.tree.render.HttpApiTreeCellRenderer;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
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
        this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.setBorder(JBUI.Borders.customLineTop(BORDER_COLOR));
        this.tree.setRootVisible(true);
        this.tree.setShowsRootHandles(true);
        this.tree.createToolTip();
        this.setViewportView(tree);
        this.tree.setCellRenderer(new HttpApiTreeCellRenderer());

        this.tree.addTreeSelectionListener(e -> {
            if (!tree.isEnabled()) {
                return;
            }
            Object component = tree.getLastSelectedPathComponent();
            if (!(component instanceof BaseNode<? extends NodeData> selectedNode)) {
                return;
            }
            Objects.requireNonNull(getChooseListener()).accept(selectedNode);
        });

        this.tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (!tree.isEnabled()) {
                    return;
                }
                BaseNode<?> node = getNode(event);
                if (Objects.isNull(node)) {
                    return;
                }
                if (SwingUtilities.isLeftMouseButton(event)) {
                    if (event.getClickCount() == 2 && Objects.nonNull(getDoubleClickListener())) {
                        getDoubleClickListener().accept(node);
                    }
                } else if (SwingUtilities.isRightMouseButton(event)) {
                    showPopupMenu(event.getX(), event.getY(), getRightClickMenu(event, node));
                }
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
    @Description("节点选中事件")
    protected abstract Consumer<BaseNode<?>> getChooseListener();

    @Nullable
    @Description("节点双击事件")
    protected abstract Consumer<BaseNode<?>> getDoubleClickListener();

    @Nullable
    @Description("节点右键菜单")
    protected abstract JPopupMenu getRightClickMenu(@NotNull MouseEvent e, @NotNull BaseNode<?> node);

    @Nullable
    public BaseNode<?> getChooseNode(@Nullable TreePath treePath) {
        Object component;
        if (Objects.nonNull(treePath)) {
            component = treePath.getLastPathComponent();
        } else {
            component = tree.getLastSelectedPathComponent();
        }
        if (!(component instanceof BaseNode<?>)) {
            return null;
        }
        return (BaseNode<?>) component;
    }

    @Description("清空树形结构中的数据")
    public void clear() {
        this.getTreeModel().setRoot(null);
    }

    @Description("展开全部")
    public void treeExpand() {
        TreePath path = tree.getSelectionPath();
        BaseNode<?> chooseNode = getChooseNode();
        if (Objects.isNull(path) || Objects.isNull(chooseNode) || chooseNode instanceof MethodNode) {
            tree.expandPath(new TreePath(tree.getModel().getRoot()));
        } else {
            expandAll(path, true);
        }
    }

    @Description("收起全部")
    public void treeCollapse() {
        expandAll(new TreePath(tree.getModel().getRoot()), false);
    }

    private void expandAll(@NotNull TreePath parent, boolean expand) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration<?> e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(path, expand);
            }
        }

        // 展开或收起必须自下而上进行
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    public void render(ModuleNode root) {
        getTreeModel().setRoot(root);
    }

    protected void showPopupMenu(int x, int y, @Nullable JPopupMenu menu) {
        if (menu == null) {
            return;
        }
        TreePath path = tree.getPathForLocation(x, y);
        tree.setSelectionPath(path);
        Rectangle rectangle = tree.getUI().getPathBounds(tree, path);
        if (rectangle != null && rectangle.contains(x, y)) {
            menu.show(tree, x, rectangle.y + rectangle.height);
        }
    }

    public BaseNode<?> getChooseNode() {
        TreePath path = tree.getSelectionPath();
        if (Objects.isNull(path)) {
            return null;
        } else {
            Object component = path.getLastPathComponent();
            return (BaseNode<?>) component;
        }
    }
}
