package com.zys.http.extension.service;

import com.intellij.DynamicBundle;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import jdk.jfr.Description;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author zhou ys
 * @since 2023-09-15
 */
@Description("国际化 i18n")
public class Bundle extends DynamicBundle {
    @NonNls
    public static final String I18N = "messages.http";

    @NotNull
    private static final Bundle INSTANCE = new Bundle();

    private Bundle() {
        super(I18N);
    }

    @Nls
    @NotNull
    protected static String message(@PropertyKey(resourceBundle = I18N) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    @Nls
    @NotNull
    public static String get(@PropertyKey(resourceBundle = I18N) String key, Object... params) {
        return message(key, params);
    }

    @Override
    protected @NotNull ResourceBundle findBundle(
            @NotNull @NonNls String pathToBundle,
            @NotNull ClassLoader loader,
            @NotNull ResourceBundle.Control control
    ) {
        final String chineseLanguagePlugin = "com.intellij.zh";
        if (!PluginManager.isPluginInstalled(PluginId.getId(chineseLanguagePlugin))) {
            // 未安装 IDE中文语言包 插件则使用默认
            return ResourceBundle.getBundle(pathToBundle, Locale.ROOT, loader, control);
        }
        return ResourceBundle.getBundle(pathToBundle, Locale.getDefault(), loader, control);
    }
}
