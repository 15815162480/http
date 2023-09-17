package com.zys.http.action;

import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.zys.http.ui.icon.HttpIcons;
import jdk.jfr.Description;

import java.awt.*;

/**
 * @author zys
 * @since 2023-09-03
 */
@Description("添加菜单")
public class AddAction extends CustomAction {
    public AddAction(String text, String description) {
        super(text, description, HttpIcons.General.ADD);

    }


}
