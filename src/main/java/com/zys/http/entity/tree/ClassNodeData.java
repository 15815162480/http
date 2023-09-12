package com.zys.http.entity.tree;

import com.intellij.psi.PsiClass;
import com.zys.http.ui.icon.HttpIcons;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ClassNodeData extends NodeData {

    private final PsiClass psiClass;

    public ClassNodeData(PsiClass psiClass) {
        super(psiClass.getName());
        this.psiClass = psiClass;
        this.setNodeIcon(HttpIcons.CLASS);
    }
}
