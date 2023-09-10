package com.zys.http.ui.tree;

import com.intellij.ide.TreeExpander;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.zys.http.entity.tree.NodeData;
import com.zys.http.ui.tree.node.BaseNode;
import com.zys.http.ui.tree.node.ClassNode;
import com.zys.http.ui.tree.render.HttpApiTreeCellRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
        this.setViewportView(tree);
        this.tree.setCellRenderer(new HttpApiTreeCellRenderer());

        // 添加节点选择监听器
        tree.addTreeSelectionListener(e -> {
            if (!tree.isEnabled()) {
                return;
            }
            Object component = tree.getLastSelectedPathComponent();
            if (!(component instanceof BaseNode<? extends NodeData>)) {
                return;
            }
            BaseNode<? extends NodeData> selectedNode = (BaseNode<? extends NodeData>) component;
            if (selectedNode instanceof ClassNode c){
                System.out.println("context-path: " + c.getValue().getContextPath());
            }
            // TODO 对选中的节点进行处理
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              // TODO 添加鼠标单击事件
            }
        });
    }

    protected final DefaultTreeModel getTreeModel() {
        return (DefaultTreeModel) tree.getModel();
    }

    @Override
    public boolean canExpand() {
        return true;
    }

    @Override
    public boolean canCollapse() {
        return true;
    }
}
