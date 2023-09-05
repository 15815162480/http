package com.zys.http.ui.table;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.DefaultTableModel;
import java.util.Vector;

/**
 * @author zys
 * @since 2023-09-03
 */
public class EnvAddTable extends AbstractTable {
    @Override
    public @NotNull DefaultTableModel initTableModel() {
        // 构建列信息
        Vector<String> columnNames = new Vector<>();
        columnNames.add("请求头");
        columnNames.add("请求值");

        Vector<Vector<String>> rowData = new Vector<>();
        rowData.add(new Vector<>(2));
        rowData.get(0).add("Content-Type");
        rowData.get(0).add("application/json");

        return new DefaultTableModel(rowData, columnNames);
    }

    @Override
    public @Nullable ActionToolbar initActionToolbar() {
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup http = (DefaultActionGroup) actionManager.getAction("http");
        return actionManager.createActionToolbar(ActionPlaces.TOOLBAR, http, true);
    }
}
