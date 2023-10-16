package com.zys.http.entity.tree;

import com.intellij.psi.NavigatablePsiElement;
import com.zys.http.constant.HttpEnum;
import com.zys.http.tool.UrlTool;
import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
@Getter
@Setter
@Description("方法结点数据")
@EqualsAndHashCode(callSuper = true)
public class MethodNodeData extends NodeData {

    private final HttpEnum.HttpMethod httpMethod;
    private String path;
    private String controllerPath;
    private String contextPath;
    private Map<String, String> headers;
    @Description("双击跳转到目标方法")
    private NavigatablePsiElement psiElement;

    public MethodNodeData(HttpEnum.HttpMethod httpMethod, String nodeName, String controllerPath, String contextPath) {
        super(nodeName);
        this.httpMethod = httpMethod;
        this.setNodeIcon(HttpIcons.HttpMethod.getHttpMethodIcon(httpMethod));
        this.contextPath = contextPath;
        this.controllerPath = controllerPath;
        this.path = nodeName;
    }

    @Override
    public String getNodeName() {
        return UrlTool.buildMethodUri(contextPath, controllerPath, path);
    }
}
