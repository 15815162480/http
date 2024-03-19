package com.zys.http.tool.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.zys.http.extension.service.Bundle;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author zhou ys
 * @since 2023-09-20
 */
@Description("信息对话框的弹出")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DialogTool {
    @Description("错误信息对话框")
    public static void error(String message) {
        ApplicationManager.getApplication().invokeLater(
                () -> Messages.showMessageDialog(message, Bundle.get("http.common.dialog.error"), Messages.getErrorIcon())
        );
    }
}
