package com.zys.http.ui.dialog;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.zys.http.service.Bundle;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Description("错误信息对话框")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorDialog {
    public static void show(String message) {
        ApplicationManager.getApplication().invokeLater(
                () -> Messages.showMessageDialog(message, Bundle.get("http.dialog.error.title"), Messages.getErrorIcon())
        );
    }
}