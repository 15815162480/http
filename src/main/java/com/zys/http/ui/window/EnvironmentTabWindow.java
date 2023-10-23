package com.zys.http.ui.window;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.UIConstant;
import com.zys.http.ui.table.EnvListTable;
import jdk.jfr.Description;

import javax.swing.*;

/**
 * @author zhou ys
 * @since 2023-10-07
 */
@Description("环境列表标签页")
public class EnvironmentTabWindow extends SimpleToolWindowPanel implements Disposable {
    private final transient Project project;
    private final EnvListTable envListTable;

    public EnvironmentTabWindow(Project project) {
        super(true, true);
        this.project = project;
        this.envListTable = new EnvListTable(project);
        envListTable.setEditOKCb(this::reloadEnv);
        JComponent component = envListTable.getToolbar().getComponent();
        component.setBorder(JBUI.Borders.customLine(UIConstant.BORDER_COLOR, 0, 0, 1, 0));
        setContent(envListTable);
    }

    public void reloadEnv() {
        DumbService.getInstance(project).smartInvokeLater(envListTable::reloadTableModel);
    }

    @Override
    public void dispose() {
        // 不处理
    }
}
