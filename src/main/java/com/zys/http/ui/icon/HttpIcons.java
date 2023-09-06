package com.zys.http.ui.icon;

import com.intellij.openapi.util.IconLoader;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.swing.*;

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
    public static final Icon REQUEST = IconLoader.getIcon("/icon/http/request.svg", HttpIcons.class);
    public static final Icon GET = IconLoader.getIcon("/icon/http/get.svg", HttpIcons.class);
    public static final Icon POST = IconLoader.getIcon("/icon/http/post.svg", HttpIcons.class);
    public static final Icon PUT = IconLoader.getIcon("/icon/http/put.svg", HttpIcons.class);
    public static final Icon DELETE = IconLoader.getIcon("/icon/http/del.svg", HttpIcons.class);
    public static final Icon PATCH = IconLoader.getIcon("/icon/http/patch.svg", HttpIcons.class);
    public static final Icon HEADER = IconLoader.getIcon("/icon/http/header.svg", HttpIcons.class);
    public static final Icon OPTIONS = IconLoader.getIcon("/icon/http/options.svg", HttpIcons.class);
}
