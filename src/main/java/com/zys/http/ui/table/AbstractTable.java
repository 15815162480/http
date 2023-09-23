package com.zys.http.ui.table;

import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.zys.http.action.AddAction;
import com.zys.http.action.CustomAction;
import com.zys.http.constant.UIConstant;
import com.zys.http.tool.HttpServiceTool;
import jdk.jfr.Description;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Objects;

/**
 * @author zys
 * @since 2023-09-03
 */
public abstract class AbstractTable extends JPanel {
    protected final transient HttpServiceTool serviceTool;
    @Description("单元格是否能编辑")
    private final boolean cellEditable;
    @Getter
    @Description("数据展示表格")
    protected JBTable valueTable;
    @Getter
    @Description("表格上方的工具栏")
    private transient ActionToolbar toolbar;

    protected AbstractTable(HttpServiceTool serviceTool, boolean cellEditable) {
        super(new BorderLayout(0, 0));
        this.cellEditable = cellEditable;
        this.serviceTool = serviceTool;
    }

    @Description("子类需要自己调用")
    protected void init() {
        this.toolbar = initToolbar();
        JBScrollPane scrollPane = initTable();
        if (Objects.nonNull(toolbar)) {
            add((Component) toolbar, BorderLayout.NORTH);
        }
        add(scrollPane, BorderLayout.CENTER);
    }


    @Description("初始化表格各个选项")
    private JBScrollPane initTable() {
        valueTable = new JBTable(this.initTableModel()) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return cellEditable;
            }
        };

        // 只能选中一行
        valueTable.setRowSelectionAllowed(true);
        valueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 是否展示表格线
        valueTable.setShowGrid(false);
        valueTable.setRowMargin(0);
        // 行高
        valueTable.setRowHeight(25);

        DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer();
        tableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        // 数据居中
        valueTable.setDefaultRenderer(Object.class, tableCellRenderer);
        // 表头居中
        valueTable.getTableHeader().setDefaultRenderer(tableCellRenderer);
        // 禁止表头拖动、选中
        valueTable.getTableHeader().setReorderingAllowed(false);
        valueTable.getTableHeader().setResizingAllowed(false);

        JBScrollPane scrollPane = new JBScrollPane(valueTable);
        scrollPane.setBorder(JBUI.Borders.customLine(UIConstant.BORDER_COLOR, 1, 1, 1, 1));
        valueTable.getModel().addTableModelListener(initTableModelListener());

        // 选中时, 工具栏的某些按钮才可以使用
        valueTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && Objects.nonNull(toolbar)) {
                toolbar.getActions().forEach(v -> {
                    if (v instanceof CustomAction c && !(v instanceof AddAction)) {
                        c.setEnabled(valueTable.getSelectedRow() != -1);
                    }
                });
            }
        });
        return scrollPane;
    }


    @Description("初始化表格上方工具栏")
    private ActionToolbar initToolbar() {
        ActionToolbar actionToolbar = initActionToolbar();
        if (Objects.isNull(actionToolbar)) {
            return null;
        }
        actionToolbar.setTargetComponent(this);
        JComponent component = actionToolbar.getComponent();
        component.setBorder(JBUI.Borders.customLine(UIConstant.BORDER_COLOR, 1, 1, 0, 1));
        component.setOpaque(true);
        return actionToolbar;
    }

    @Description("获取表格数据对象")
    public DefaultTableModel getTableModel() {
        return (DefaultTableModel) valueTable.getModel();
    }

    @Description("重新加载表格数据")
    public void reloadTableModel() {
        valueTable.setModel(initTableModel());
    }

    @Description("初始化表格的数据")
    protected abstract @NotNull DefaultTableModel initTableModel();

    @Description("初始化工具栏")
    protected abstract @Nullable ActionToolbar initActionToolbar();

    @Description("初始化表格模型数据变化监听器")
    protected abstract @NotNull TableModelListener initTableModelListener();
}
