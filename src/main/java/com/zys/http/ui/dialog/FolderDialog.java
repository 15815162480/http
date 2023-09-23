package com.zys.http.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author zhou ys
 * @since 2023-09-22
 */
public class FolderDialog extends DialogWrapper {
    public FolderDialog(@Nullable Project project, boolean canBeParent) {
        super(project, canBeParent);
        init();
        setTitle("11");
        getRootPane().setMinimumSize(new Dimension(500, 200));
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return null;
    }
}
