package com.zys.http.entity.tree;

import com.intellij.psi.PsiClass;
import com.zys.http.ui.icon.HttpIcons;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
public class ClassNodeData extends NodeData {
    private PsiClass psiClass;

    public ClassNodeData() {
        this.setNodeIcon(HttpIcons.CLASS);
    }
}
