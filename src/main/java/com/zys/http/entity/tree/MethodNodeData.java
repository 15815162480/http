package com.zys.http.entity.tree;

import com.intellij.psi.NavigatablePsiElement;
import com.zys.http.constant.HttpEnum;
import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;

import java.util.Map;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
public class MethodNodeData extends NodeData {

    private String path;

    private Map<String, String> headers;

    @Description("双击跳转到目标方法")
    private NavigatablePsiElement psiElement;

    private final HttpEnum.HttpMethod httpMethod;

    public MethodNodeData(HttpEnum.HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        this.setNodeIcon(HttpIcons.getHttpMethodIcon(httpMethod));
    }
}
