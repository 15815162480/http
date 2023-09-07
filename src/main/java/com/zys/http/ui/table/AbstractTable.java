package com.zys.http.ui.table;

import com.intellij.execution.util.StringWithNewLinesCellEditor;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.zys.http.action.AddAction;
import com.zys.http.action.CustomAction;
import com.zys.http.constant.UIConstant;
import com.zys.http.util.HttpPropertyTool;
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
    @Getter
    @Description("表格上方的工具栏")
    private transient ActionToolbar toolbar;

    @Getter
    @Description("数据展示表格")
    protected JBTable valueTable;

    @Description("表格所在区域")
    private JBScrollPane scrollPane;

    @Description("所在项目")
    protected transient Project project;

    @Description("单元格是否能编辑")
    private final boolean cellEditable;

    @Getter
    @Description("存储工具")
    protected final transient HttpPropertyTool httpPropertyTool;

    @Description("表格的默认展示宽高")
    public static final Dimension PREFERRED_DIMENSION = new Dimension(300, 200);

    protected AbstractTable(Project project, boolean cellEditable) {
        super(new GridBagLayout());
        this.project = project;
        this.cellEditable = cellEditable;
        this.httpPropertyTool = HttpPropertyTool.getInstance(this.project);
    }

    @Description("子类需要自己调用")
    protected void init() {
        initToolbar();
        initTable();
        initLayout();
    }


    private void initTable() {
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

        scrollPane = new JBScrollPane(valueTable);
        scrollPane.setPreferredSize(PREFERRED_DIMENSION);
        scrollPane.setBorder(JBUI.Borders.customLine(UIConstant.BORDER_COLOR, 1, 1, 1, 1));

        // 单元格失去焦点事件
        StringWithNewLinesCellEditor editor = new StringWithNewLinesCellEditor();
        JTextField field = (JTextField) editor.getComponent();
        field.setBorder(null);
        valueTable.setCellEditor(editor);

        valueTable.getModel().addTableModelListener(initTableModelListener());

        // 选中时, 工具栏的某些按钮才可以使用
        valueTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                toolbar.getActions().forEach(v -> {
                    if (v instanceof CustomAction c && !(v instanceof AddAction)) {
                        c.setEnabled(valueTable.getSelectedRow() != -1);
                    }
                });
            }
        });
    }


    private void initToolbar() {
        this.toolbar = initActionToolbar();
        if (Objects.isNull(this.toolbar)) {
            return;
        }
        toolbar.setTargetComponent(this);
        JComponent component = toolbar.getComponent();
        component.setBorder(JBUI.Borders.customLine(UIConstant.BORDER_COLOR, 1, 1, 0, 1));
        component.setOpaque(true);
    }

    private void initLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = JBUI.emptyInsets();
        gbc.weightx = 1.0;
        if (Objects.nonNull(toolbar)) {
            add((Component) toolbar, gbc);
            gbc.gridy = 1;
        }
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollPane, gbc);
    }


    @Description("初始化表格的数据")
    protected abstract @NotNull DefaultTableModel initTableModel();

    @Description("初始化工具栏")
    protected abstract @Nullable ActionToolbar initActionToolbar();

    @Description("初始化表格模型数据变化监听器")
    protected abstract @NotNull TableModelListener initTableModelListener();
}
