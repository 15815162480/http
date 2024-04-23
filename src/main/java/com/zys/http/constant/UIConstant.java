package com.zys.http.constant;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.*;

/**
 * @author zys
 * @since 2023-08-27
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UIConstant {
    @Description("边框颜色")
    public static final JBColor BORDER_COLOR = new JBColor(Gray._214, new Color(30, 31, 34));
}
