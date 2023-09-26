package com.zys.http.entity.tree;

import com.intellij.psi.PsiClass;
import com.zys.http.tool.ui.ThemeTool;
import com.zys.http.ui.icon.HttpIcons;
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
        this.setNodeIcon(ThemeTool.isDark() ? HttpIcons.TreeNode.CLASS : HttpIcons.TreeNode.CLASS_LIGHT);
    }
}
