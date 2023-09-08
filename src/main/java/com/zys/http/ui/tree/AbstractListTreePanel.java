package com.zys.http.ui.tree;

import com.intellij.ide.TreeExpander;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.zys.http.entity.tree.NodeData;
import com.zys.http.ui.tree.node.BaseNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author zhou ys
 * @since 2023-09-05
 */
public abstract class AbstractListTreePanel extends JBScrollPane implements TreeExpander {
    private final JTree tree;

    protected AbstractListTreePanel(@NotNull final JTree tree) {
        this.tree = tree;
        this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.setBorder(new CustomLineBorder(JBUI.insetsTop(1)));

        this.tree.setRootVisible(true);
        this.tree.setShowsRootHandles(false);
        this.setViewportView(tree);

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

            // TODO 对选中的节点进行处理
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              // TODO 添加鼠标单击事件
            }
        });
    }
}
