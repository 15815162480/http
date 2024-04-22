package com.zys.http.ui.icon;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
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
    public static final Icon PLUGIN_ICON;

    static {
        // 图标插件 Extra ToolWindow Colorful Icons
        if (PluginManager.isPluginInstalled(PluginId.getId("lermitage.intellij.extratci"))) {
            PLUGIN_ICON = IconLoader.getIcon("/icon/plugins_color.svg", HttpIcons.class);
        } else {
            PLUGIN_ICON = IconLoader.getIcon("/icon/plugins.svg", HttpIcons.class);
        }
    }

    @Description("通用")
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class General {
        public static final Icon DEFAULT = IconLoader.getIcon("/icon/general/default.svg", HttpIcons.class);
        public static final Icon FULL_SCREEN = IconLoader.getIcon("/icon/general/fullScreen.svg", HttpIcons.class);
    }

    @Description("请求方法")
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HttpMethod {
        public static final Icon REQUEST = IconLoader.getIcon("/icon/http/request.svg", HttpIcons.class);
        public static final Icon GET = IconLoader.getIcon("/icon/http/get.svg", HttpIcons.class);
        public static final Icon POST = IconLoader.getIcon("/icon/http/post.svg", HttpIcons.class);
        public static final Icon PUT = IconLoader.getIcon("/icon/http/put.svg", HttpIcons.class);
        public static final Icon DELETE = IconLoader.getIcon("/icon/http/del.svg", HttpIcons.class);
        public static final Icon PATCH = IconLoader.getIcon("/icon/http/patch.svg", HttpIcons.class);
        // 请求方法
        private static final EnumMap<HttpEnum.HttpMethod, Icon> HTTP_METHOD_ICON_MAP = new EnumMap<>(HttpEnum.HttpMethod.class);

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
}
