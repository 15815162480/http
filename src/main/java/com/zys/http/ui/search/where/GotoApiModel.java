package com.zys.http.ui.search.where;

import com.intellij.ide.DataManager;
import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.Utils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.zys.http.ui.search.ApiSearchItem;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author zys
 * @since 2023-10-10
 */
public class GotoApiModel implements ChooseByNameModel, DumbAware {
    private final Project project;
    private final DataContext dataContext;

    private final Editor editor;

    public GotoApiModel(Project project, Component component,Editor editor) {
        this.project = project;
        this.dataContext = Utils.wrapDataContext(DataManager.getInstance().getDataContext(component));
        this.editor = editor;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) String getPromptText() {
        return "Enter API";
    }

    @Override
    public @NotNull String getNotInMessage() {
        return "No found";
    }

    @Override
    public @NotNull String getNotFoundMessage() {
        return "No found";
    }

    @Override
    public @Nullable String getCheckBoxName() {
        return "Checkbox";
    }

    @Override
    public boolean loadInitialCheckBoxState() {
        return false;
    }

    @Override
    public void saveInitialCheckBoxState(boolean state) {
        // 不处理
    }

    @Override
    public @NotNull ListCellRenderer<?> getListCellRenderer() {
        return new DefaultListCellRenderer();
    }

    @Override
    public String @NotNull @Nls [] getNames(boolean checkBoxState) {
        return new String[0];
    }

    @Override
    public Object @NotNull [] getElementsByName(@NotNull String name, boolean checkBoxState, @NotNull String pattern) {
        return new Object[0];
    }

    @Override
    public @Nullable String getElementName(@NotNull Object element) {
        System.out.println("element.getClass().getName() = " + element.getClass().getName());
        return ((ApiSearchItem) element).getMethodNodeData().getNodeName();
    }

    @Override
    public String @NotNull [] getSeparators() {
        return new String[]{"/"};
    }

    @Override
    public @Nullable String getFullName(@NotNull Object element) {
        return getElementName(element);
    }

    @Override
    public @Nullable @NonNls String getHelpId() {
        return "api.tool";
    }

    @Override
    public boolean willOpenEditor() {
        return true;
    }

    @Override
    public boolean useMiddleMatching() {
        return false;
    }

}
