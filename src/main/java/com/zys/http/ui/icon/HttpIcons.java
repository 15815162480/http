package com.zys.http.ui.icon;

import com.intellij.openapi.util.IconLoader;
import com.zys.http.constant.HttpEnum;
import com.zys.http.tool.ColorsTool;
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

    @Description("插件工具栏上展示的图标")
    public static final Icon PLUGIN_ICON = IconLoader.getIcon("/icon/plugins.svg", HttpIcons.class);

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
        private static final EnumMap<HttpEnum.HttpMethod, Icon> HTTP_METHOD_ICON_DARK_MAP = new EnumMap<>(HttpEnum.HttpMethod.class);
        private static final EnumMap<HttpEnum.HttpMethod, Icon> HTTP_METHOD_ICON_LIGHT_MAP = new EnumMap<>(HttpEnum.HttpMethod.class);
        public static final Icon REQUEST = IconLoader.getIcon("/icon/http/dark/request.svg", HttpIcons.class);
        public static final Icon GET = IconLoader.getIcon("/icon/http/dark/get.svg", HttpIcons.class);
        public static final Icon POST = IconLoader.getIcon("/icon/http/dark/post.svg", HttpIcons.class);
        public static final Icon PUT = IconLoader.getIcon("/icon/http/dark/put.svg", HttpIcons.class);
        public static final Icon DELETE = IconLoader.getIcon("/icon/http/dark/del.svg", HttpIcons.class);
        public static final Icon PATCH = IconLoader.getIcon("/icon/http/dark/patch.svg", HttpIcons.class);
        public static final Icon HEADER = IconLoader.getIcon("/icon/http/dark/header.svg", HttpIcons.class);
        public static final Icon OPTIONS = IconLoader.getIcon("/icon/http/dark/options.svg", HttpIcons.class);

        public static final Icon REQUEST_LIGHT = IconLoader.getIcon("/icon/http/light/request.svg", HttpIcons.class);
        public static final Icon GET_LIGHT = IconLoader.getIcon("/icon/http/light/get.svg", HttpIcons.class);
        public static final Icon POST_LIGHT = IconLoader.getIcon("/icon/http/light/post.svg", HttpIcons.class);
        public static final Icon PUT_LIGHT = IconLoader.getIcon("/icon/http/light/put.svg", HttpIcons.class);
        public static final Icon DELETE_LIGHT = IconLoader.getIcon("/icon/http/light/del.svg", HttpIcons.class);
        public static final Icon PATCH_LIGHT = IconLoader.getIcon("/icon/http/light/patch.svg", HttpIcons.class);
        public static final Icon HEADER_LIGHT = IconLoader.getIcon("/icon/http/light/header.svg", HttpIcons.class);
        public static final Icon OPTIONS_LIGHT = IconLoader.getIcon("/icon/http/light/options.svg", HttpIcons.class);

        static {
            HTTP_METHOD_ICON_DARK_MAP.put(HttpEnum.HttpMethod.GET, GET);
            HTTP_METHOD_ICON_DARK_MAP.put(HttpEnum.HttpMethod.REQUEST, REQUEST);
            HTTP_METHOD_ICON_DARK_MAP.put(HttpEnum.HttpMethod.POST, POST);
            HTTP_METHOD_ICON_DARK_MAP.put(HttpEnum.HttpMethod.PUT, PUT);
            HTTP_METHOD_ICON_DARK_MAP.put(HttpEnum.HttpMethod.DELETE, DELETE);
            HTTP_METHOD_ICON_DARK_MAP.put(HttpEnum.HttpMethod.PATCH, PATCH);

            HTTP_METHOD_ICON_LIGHT_MAP.put(HttpEnum.HttpMethod.GET, GET_LIGHT);
            HTTP_METHOD_ICON_LIGHT_MAP.put(HttpEnum.HttpMethod.REQUEST, REQUEST_LIGHT);
            HTTP_METHOD_ICON_LIGHT_MAP.put(HttpEnum.HttpMethod.POST, POST_LIGHT);
            HTTP_METHOD_ICON_LIGHT_MAP.put(HttpEnum.HttpMethod.PUT, PUT_LIGHT);
            HTTP_METHOD_ICON_LIGHT_MAP.put(HttpEnum.HttpMethod.DELETE, DELETE_LIGHT);
            HTTP_METHOD_ICON_LIGHT_MAP.put(HttpEnum.HttpMethod.PATCH, PATCH_LIGHT);
        }

        public static Icon getHttpMethodIcon(HttpEnum.HttpMethod httpMethod) {
            return ColorsTool.isDark() ? HTTP_METHOD_ICON_DARK_MAP.getOrDefault(httpMethod, REQUEST)
                    : HTTP_METHOD_ICON_LIGHT_MAP.getOrDefault(httpMethod, REQUEST_LIGHT);
        }
    }

    @Description("树形结点")
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TreeNode {
        public static final Icon MODULE = IconLoader.getIcon("/icon/tree/module_dark.svg", HttpIcons.class);
        public static final Icon PACKAGE = IconLoader.getIcon("/icon/tree/package_dark.svg", HttpIcons.class);
        public static final Icon CLASS = IconLoader.getIcon("/icon/tree/class_dark.svg", HttpIcons.class);

        public static final Icon MODULE_LIGHT = IconLoader.getIcon("/icon/tree/module_light.svg", HttpIcons.class);
        public static final Icon PACKAGE_LIGHT = IconLoader.getIcon("/icon/tree/package_light.svg", HttpIcons.class);
        public static final Icon CLASS_LIGHT = IconLoader.getIcon("/icon/tree/class_light.svg", HttpIcons.class);
    }
}
