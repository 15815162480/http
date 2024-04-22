package com.zys.http.entity.tree;

import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiClass;
import jdk.jfr.Description;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
@Getter
@Description("类结点数据")
@EqualsAndHashCode(callSuper = true)
public class ClassNodeData extends NodeData {
    private final PsiClass psiClass;

    public ClassNodeData(PsiClass psiClass) {
        super(psiClass.getName());
        this.psiClass = psiClass;
        this.setNodeIcon(AllIcons.Nodes.Class);
    }
}
