package com.zys.http.ui.table;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.zys.http.action.AddAction;
import com.zys.http.action.CustomAction;
import com.zys.http.action.RemoveAction;
import com.zys.http.service.Bundle;
import com.zys.http.service.NotifyService;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.util.Arrays;

/**
 * @author zhou ys
 * @since 2023-10-19
 */
public class FileUploadTable extends AbstractTable {
    public FileUploadTable(Project project) {
        super(project, false);
        init();
    }

    @Override
    protected @NotNull DefaultTableModel initTableModel() {
        String[] columnNames = {"文件路径"};
        return new DefaultTableModel(null, columnNames);
    }

    @Override
    protected @Nullable ActionToolbar initActionToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        AddAction addAction = new AddAction(Bundle.get("http.action.add"));
        addAction.setAction(event -> {
            DefaultTableModel model = (DefaultTableModel) valueTable.getModel();
            VirtualFile[] virtualFiles = createFileChooser(project);
            if (virtualFiles.length > 0) {
                String[] fileNames = Arrays.stream(virtualFiles).map(VirtualFile::getPath).toList().toArray(new String[]{});
                for (String fileName : fileNames) {
                    model.addRow(new String[]{fileName});
                }
            }
        });
        group.add(addAction);

        RemoveAction removeAction = new RemoveAction(Bundle.get("http.action.remove"));
        removeAction.setAction(event -> {
            int selectedRow = valueTable.getSelectedRow();
            getTableModel().removeRow(selectedRow);
            int rowCount = valueTable.getRowCount();
            int newSelectRow = selectedRow == rowCount ? rowCount - 1 : selectedRow;
            valueTable.clearSelection();
            valueTable.getSelectionModel().setSelectionInterval(newSelectRow, newSelectRow);
        });
        removeAction.setEnabled(false);
        group.add(removeAction);

        return ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true);
    }

    @Override
    protected @NotNull TableModelListener initTableModelListener() {
        return e -> {
            if (e.getType() != TableModelEvent.DELETE || ((DefaultTableModel) e.getSource()).getRowCount() > 0) {
                return;
            }
            getToolbar().getActions().forEach(v -> {
                if (v instanceof CustomAction c && !(v instanceof AddAction)) {
                    c.setEnabled(false);
                }
            });
        };
    }

    @Override
    protected @NotNull ListSelectionListener initListSelectionListener() {
        return e -> getToolbar().getActions().forEach(v -> {
            if (v instanceof CustomAction c) {
                c.setEnabled(v instanceof AddAction || valueTable.getSelectedRow() != -1);
            }
        });
    }

    public String[] fileNames() {
        DefaultTableModel model = getTableModel();
        int rowCount = model.getRowCount();
        String[] fileNames = new String[rowCount];
        for (int i = 0; i < rowCount; i++) {
            fileNames[i] = (String) model.getValueAt(i, 0);
        }
        return fileNames;
    }

    @Description("创建文件选择对话框")
    private VirtualFile[] createFileChooser(Project project) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, true);
        descriptor.setTitle("选择上传的文件列表");
        FileChooserFactory.getInstance().createFileChooser(descriptor, project, null);
        VirtualFile[] selectedFiles = FileChooser.chooseFiles(descriptor, project, null);
        if (selectedFiles.length < 1) {
            NotifyService.instance(project).error("未选择文件");
            return new VirtualFile[0];
        }
        return selectedFiles;
    }
}
