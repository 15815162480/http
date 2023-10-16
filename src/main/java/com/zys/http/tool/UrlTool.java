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
        String uri = contextPath.isEmpty() ? "/" : contextPath;
        String text = controllerPath.endsWith("/") ? controllerPath.substring(0, controllerPath.length() - 2) : controllerPath;
        if ("/".equals(uri)) {
            uri += !text.startsWith("/") ? text : text.substring(1);
        } else {
            uri += !text.startsWith("/") ? "/" + text : text;
        }
        uri += methodPath.startsWith("/") ? methodPath : "/" + methodPath;
        return uri;
    }
}
