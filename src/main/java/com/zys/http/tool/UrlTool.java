package com.zys.http.tool;

import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author zhou ys
 * @since 2023-10-13
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlTool {
    @Description("构建方法请求 URI")
    public static String buildMethodUri(String contextPath, String controllerPath, String methodPath) {
        // context-path 只允许以 / 开头且不允许 / 结尾
        StringBuilder uri = new StringBuilder();
        contextPath = contextPath.isEmpty() ? "/" : contextPath;
        uri.append(contextPath);

        controllerPath = controllerPath.endsWith("/") && controllerPath.length() > 1 ?
                controllerPath.substring(0, controllerPath.length() - 1) : controllerPath;

        if ("/".equals(contextPath)) {
            uri.append(controllerPath.startsWith("/") ? controllerPath.substring(1) : controllerPath);
        } else {
            uri.append(controllerPath.startsWith("/") ? controllerPath : "/" + controllerPath);
        }

        if (controllerPath.endsWith("/") && methodPath.startsWith("/")) {
            uri.append(methodPath.substring(1));
        } else {
            uri.append(methodPath.startsWith("/") ? methodPath : "/" + methodPath);
        }

        return uri.toString();
    }
}
