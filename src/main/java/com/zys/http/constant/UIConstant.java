package com.zys.http.constant;

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
    @Description("目前仅适配暗黑模式, 如需适配 light 调整第一个参数")
    public static final JBColor BORDER_COLOR = new JBColor(JBColor.darkGray, new Color(30, 31, 34));
}
