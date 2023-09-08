package com.zys.http.ui.icon;

import com.intellij.openapi.util.IconLoader;
import com.zys.http.constant.HttpEnum;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.swing.*;
import java.util.EnumMap;

/**
 * @author zhou ys
 * @since 2023-09-06
 */
@Description("自定义图标")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpIcons {
    public static final Icon ENVIRONMENT = IconLoader.getIcon("/icon/environment.svg", HttpIcons.class);
    public static final Icon ADD = IconLoader.getIcon("/icon/add.svg", HttpIcons.class);
    public static final Icon REMOVE = IconLoader.getIcon("/icon/remove.svg", HttpIcons.class);

    // 请求方法
    private static final EnumMap<HttpEnum.HttpMethod, Icon> HTTP_METHOD_ICON_MAP = new EnumMap<>(HttpEnum.HttpMethod.class);
    public static final Icon REQUEST = IconLoader.getIcon("/icon/http/request.svg", HttpIcons.class);
    public static final Icon GET = IconLoader.getIcon("/icon/http/get.svg", HttpIcons.class);
    public static final Icon POST = IconLoader.getIcon("/icon/http/post.svg", HttpIcons.class);
    public static final Icon PUT = IconLoader.getIcon("/icon/http/put.svg", HttpIcons.class);
    public static final Icon DELETE = IconLoader.getIcon("/icon/http/del.svg", HttpIcons.class);
    public static final Icon PATCH = IconLoader.getIcon("/icon/http/patch.svg", HttpIcons.class);
    public static final Icon HEADER = IconLoader.getIcon("/icon/http/header.svg", HttpIcons.class);
    public static final Icon OPTIONS = IconLoader.getIcon("/icon/http/options.svg", HttpIcons.class);

    static {
        HTTP_METHOD_ICON_MAP.put(HttpEnum.HttpMethod.GET, GET);
        HTTP_METHOD_ICON_MAP.put(HttpEnum.HttpMethod.REQUEST, REQUEST);
        HTTP_METHOD_ICON_MAP.put(HttpEnum.HttpMethod.POST, POST);
        HTTP_METHOD_ICON_MAP.put(HttpEnum.HttpMethod.PUT, PUT);
        HTTP_METHOD_ICON_MAP.put(HttpEnum.HttpMethod.DELETE, DELETE);
        HTTP_METHOD_ICON_MAP.put(HttpEnum.HttpMethod.PATCH, PATCH);
    }

    public static Icon getHttpMethodIcon(HttpEnum.HttpMethod httpMethod) {
        return HTTP_METHOD_ICON_MAP.getOrDefault(httpMethod, REQUEST);
    }

    // 树形结构
    public static final Icon MODULE = IconLoader.getIcon("/icon/tree/module.svg", HttpIcons.class);
    public static final Icon PACKAGE = IconLoader.getIcon("/icon/tree/package.svg", HttpIcons.class);
    public static final Icon CLASS = IconLoader.getIcon("/icon/tree/class.svg", HttpIcons.class);
}
