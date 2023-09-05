package com.zys.http.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.zys.http.ui.table.EnvShowTable;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author zhou ys
 * @since 2023-09-05
 */
@Description("环境列表对话框")
public class EnvShowDialog extends DialogWrapper {

    @Description("环境列表表格")
    private final EnvShowTable envShowTable;

    public EnvShowDialog(@NotNull Project project) {
        super(project, true);
        envShowTable = new EnvShowTable(project);
        init();
        getRootPane().setMinimumSize(new Dimension(500, 200));
        setTitle("环境列表");
        setCancelButtonText("取消");
        setOKButtonText("确定");
        setAutoAdjustable(true);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel main = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        // 环境列表展示
        main.add(envShowTable, gbc);
        return main;
    }
}
