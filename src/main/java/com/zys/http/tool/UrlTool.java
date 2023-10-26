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
        contextPath = contextPath.isEmpty() || "/".equals(contextPath) ? "" : contextPath;
        uri.append(contextPath);

        controllerPath = removePrefixSlash(controllerPath);
        controllerPath = removeSuffixSlash(controllerPath);

        methodPath = removePrefixSlash(methodPath);
        methodPath = removeSuffixSlash(methodPath);

        if (controllerPath.isEmpty() && methodPath.isEmpty()) {
            return uri.toString();
        }
        if (controllerPath.isEmpty()) {
            uri.append("/").append(methodPath);
            return uri.toString();
        }
        if (methodPath.isEmpty()) {
            uri.append("/").append(controllerPath);
            return uri.toString();
        }
        uri.append("/").append(controllerPath).append("/").append(methodPath);

        return uri.toString();
    }

    private static String removePrefixSlash(String s) {
        if (s.length() == 1 && "/".equals(s)) {
            return "";
        }
        return s.startsWith("/") ? s.substring(1) : s;
    }

    private static String removeSuffixSlash(String s) {
        if (s.length() == 1 && "/".equals(s)) {
            return "";
        }
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
