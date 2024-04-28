package com.zys.http.ui.table;

import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.zys.http.constant.UIConstant;
import com.zys.http.tool.HttpServiceTool;
import jdk.jfr.Description;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Objects;

/**
 * @author zys
 * @since 2023-09-03
 */
@Description("带有工具栏的表格")
public abstract class AbstractTable extends JPanel {

    protected final transient Project project;
    protected final transient HttpServiceTool serviceTool;

    @Description("单元格是否能编辑")
    private final boolean cellEditable;
    @Description("表格头是否能调整")
    private final boolean headerResized;
    @Description("表格是否需要边框")
    private final boolean needBorder;

    @Getter
    @Description("数据展示表格")
    protected JBTable valueTable;

    @Getter
    @Description("表格上方的工具栏")
    private transient ActionToolbar toolbar;

    protected AbstractTable(Project project, boolean cellEditable, boolean headerCanResized) {
        this(project, cellEditable, headerCanResized, true);
    }

    protected AbstractTable(Project project, boolean cellEditable, boolean headerCanResized, boolean needBorder) {
        super(new BorderLayout(0, 0));
        this.project = project;
        this.cellEditable = cellEditable;
        this.headerResized = headerCanResized;
        this.needBorder = needBorder;
        this.serviceTool = HttpServiceTool.getInstance(project);
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

    @Description("初始化表格")
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
        valueTable.getTableHeader().setResizingAllowed(headerResized);

        JBScrollPane scrollPane = new JBScrollPane(valueTable);
        if (needBorder) {
            scrollPane.setBorder(JBUI.Borders.customLine(UIConstant.BORDER_COLOR, 1, 1, 1, 1));
        } else {
            scrollPane.setBorder(JBUI.Borders.empty());
        }
        valueTable.getModel().addTableModelListener(initTableModelListener());

        // 选中时, 工具栏的某些按钮才可以使用
        valueTable.getSelectionModel().addListSelectionListener(initListSelectionListener());
        return scrollPane;
    }

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

    public DefaultTableModel getTableModel() {
        return (DefaultTableModel) valueTable.getModel();
    }

    public void reloadTableModel() {
        valueTable.setModel(initTableModel());
        valueTable.getModel().addTableModelListener(initTableModelListener());
    }

    @Description("初始化表格的数据")
    protected abstract @NotNull DefaultTableModel initTableModel();

    @Description("初始化工具栏")
    protected abstract @Nullable ActionToolbar initActionToolbar();

    @Description("初始化表格模型数据变化监听器")
    protected abstract @NotNull TableModelListener initTableModelListener();

    @Description("初始化表格模型选择监听器")
    protected abstract @NotNull ListSelectionListener initListSelectionListener();
}
