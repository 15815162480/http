package com.zys.http.ui.window;

import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.UIConstant;
import com.zys.http.tool.SystemTool;
import com.zys.http.ui.table.EnvListTable;
import jdk.jfr.Description;

import javax.swing.*;

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
        envListTable.setEditOKCb(this::reloadEnv);
        JComponent component = envListTable.getToolbar().getComponent();
        component.setBorder(JBUI.Borders.customLine(UIConstant.BORDER_COLOR, 0, 0, 1, 0));
        setContent(envListTable);
    }

    public void reloadEnv() {
        SystemTool.schedule(envListTable::reloadTableModel, 800);
    }
}
