package com.zys.http.ui.window;

import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.UIConstant;
import com.zys.http.ui.table.EnvListTable;
import jdk.jfr.Description;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author zhou ys
 * @since 2023-10-07
 */
@Description("环境列表标签页")
public class EnvironmentTabWindow extends SimpleToolWindowPanel {
    private final EnvListTable envListTable;

    public EnvironmentTabWindow(EnvListTable envListTable) {
        super(true, true);
        this.envListTable = envListTable;
        envListTable.setEditOKCb(v -> reloadEnv());
        JComponent component = envListTable.getToolbar().getComponent();
        component.setBorder(JBUI.Borders.customLine(UIConstant.BORDER_COLOR, 0, 0, 1, 0));
        setContent(envListTable);
    }

    public void reloadEnv() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                envListTable.reloadTableModel();
            }
        }, 800);
    }
}
