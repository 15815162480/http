package com.zys.http.tool.ui;

import com.intellij.ide.ui.UIDensity;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.util.registry.RegistryManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author zys
 * @since 2023-09-17
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThemeTool {

    private static Boolean newUIEnabled;

    public static synchronized boolean isNewUI() {
        if (newUIEnabled == null) {
            try {
                newUIEnabled = RegistryManager.getInstance().get("ide.experimental.ui").asBoolean();
            } catch (Exception e) {
                newUIEnabled = true;
            }
        }

        return newUIEnabled;
    }


    public static synchronized boolean isCompactMode() {
        isNewUI();
        if (!newUIEnabled) {
            return false;
        }
        return UISettings.Companion.getInstance().getUiDensity() == UIDensity.COMPACT;
    }
}
