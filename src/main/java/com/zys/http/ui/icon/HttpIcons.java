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

    @Description("通用")
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class General {
        public static final Icon ENVIRONMENT = IconLoader.getIcon("/icon/general/environment.svg", HttpIcons.class);
        public static final Icon ADD = IconLoader.getIcon("/icon/general/add.svg", HttpIcons.class);
        public static final Icon REMOVE = IconLoader.getIcon("/icon/general/remove.svg", HttpIcons.class);
        public static final Icon REFRESH = IconLoader.getIcon("/icon/general/refresh.svg", HttpIcons.class);
        public static final Icon EXPAND = IconLoader.getIcon("/icon/general/expand.svg", HttpIcons.class);
        public static final Icon COLLAPSE = IconLoader.getIcon("/icon/general/collapse.svg", HttpIcons.class);
        public static final Icon FILTER = IconLoader.getIcon("/icon/general/filter.svg", HttpIcons.class);
        public static final Icon LIST = IconLoader.getIcon("/icon/general/list.svg", HttpIcons.class);
        public static final Icon DEFAULT = IconLoader.getIcon("/icon/general/default.svg", HttpIcons.class);
    }

    @Description("请求方法")
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HttpMethod {
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
    }

    @Description("树形结点")
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TreeNode {
        public static final Icon MODULE = IconLoader.getIcon("/icon/tree/module.svg", HttpIcons.class);
        public static final Icon PACKAGE = IconLoader.getIcon("/icon/tree/package.svg", HttpIcons.class);
        public static final Icon CLASS = IconLoader.getIcon("/icon/tree/class.svg", HttpIcons.class);
    }
}
