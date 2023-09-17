package com.zys.http.tool;

import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.*;

/**
 * @author zys
 * @since 2023-09-17
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ColorsTool {

    public static boolean isDark() {
        EditorColorsManager colorsManager = EditorColorsManager.getInstance();
        EditorColorsScheme scheme = colorsManager.getGlobalScheme();
        Color caretRowColor = scheme.getColor(EditorColors.CARET_ROW_COLOR);
        if (caretRowColor != null) {
            float brightness = calculateBrightness(caretRowColor);
            return brightness < 0.5;
        }
        return true;
    }
    private static float calculateBrightness(Color color) {
        return (color.getRed() * 299 + color.getGreen() * 587 + color.getBlue() * 114) / 1000f / 255f;
    }
}
