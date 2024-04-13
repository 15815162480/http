package com.zys.http.ui.window;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.UIConstant;
import com.zys.http.ui.table.HistoryListTable;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author zhou ys
 * @since 2024-03-01
 */
@Description("请求历史记录")
public class HistoryTabWindow extends SimpleToolWindowPanel implements Disposable {

    public HistoryTabWindow(@NotNull Project project) {
        super(true);
        HistoryListTable historyListTable = new HistoryListTable(project);
        JComponent component = historyListTable.getToolbar().getComponent();
        component.setBorder(JBUI.Borders.customLine(UIConstant.BORDER_COLOR, 0, 0, 1, 0));
        setContent(historyListTable);
    }

    @Override
    public void dispose() {
        // 不处理
    }
}
