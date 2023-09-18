package com.zys.http.ui.table;

import com.intellij.openapi.project.Project;
import com.zys.http.service.Bundle;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.DefaultTableModel;

/**
 * @author zys
 * @since 2023-09-16
 */
@Description("请求参数(path、param 参数)表格")
public class ParameterTable extends EnvHeaderTable {
    public ParameterTable(Project project) {
        super(project, true, "");
    }

    @Override
    protected @NotNull DefaultTableModel initTableModel() {
        Object[] columnNames = {
                Bundle.get("http.table.param.key"),
                Bundle.get("http.table.param.value")
        };
        return new DefaultTableModel(null, columnNames);
    }
}