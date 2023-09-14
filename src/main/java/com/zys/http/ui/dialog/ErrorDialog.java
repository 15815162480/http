package com.zys.http.ui.dialog;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorDialog {
    public static void show(String message) {
        ApplicationManager.getApplication().invokeLater(() -> Messages.showMessageDialog(message, "Error", Messages.getErrorIcon()));
    }
}