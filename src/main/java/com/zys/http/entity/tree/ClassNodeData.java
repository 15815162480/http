package com.zys.http.entity.tree;

import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import jdk.jfr.Description;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.kotlin.psi.KtClass;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
@Getter
@Description("类结点数据")
@EqualsAndHashCode(callSuper = true)
public class ClassNodeData extends NodeData {
    private final PsiElement psiElement;

    public ClassNodeData(PsiElement psiElement) {
        super(psiElement instanceof PsiClass psiClass ? psiClass.getName() : ((KtClass) psiElement).getName());
        this.psiElement = psiElement;
        this.setNodeIcon(AllIcons.Nodes.Class);
    }
}
