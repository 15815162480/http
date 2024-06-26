package com.zys.http.extension.search.everywhere;

import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeUICustomization;
import com.zys.http.constant.HttpEnum;
import com.zys.http.entity.tree.MethodNodeData;
import jdk.jfr.Description;
import lombok.Getter;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author zys
 * @since 2023-10-10
 */
@Getter
@Description("数据模型")
public class GotoApiModel extends FilteringGotoByModel<HttpEnum.HttpMethod> implements DumbAware {

    private final List<MethodNodeData> nodeDataList;

    public GotoApiModel(Project project, GotoApiChooseByNameContributor contributor) {
        super(project, new ChooseByNameContributor[]{contributor});
        this.nodeDataList = contributor.dataList();
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) String getPromptText() {
        return IdeUICustomization.getInstance().projectMessage("checkbox.include.non.project.items", new Object[0]);
    }

    @Override
    public @NotNull String getNotInMessage() {
        return "No api found";
    }

    @Override
    public @NotNull String getNotFoundMessage() {
        return "No api found";
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
    public Object @NotNull [] getElementsByName(@NotNull String name, boolean checkBoxState, @NotNull String pattern) {
        return nodeDataList.stream()
                .filter(v -> v.getNodeName().equals(name) || v.getNodeName().contains(pattern))
                .map(GotoApiItem::new)
                .toList().toArray(new GotoApiItem[0]);
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
    public boolean willOpenEditor() {
        return true;
    }

    @Override
    protected @Nullable HttpEnum.HttpMethod filterValueFor(NavigationItem item) {
        return HttpEnum.HttpMethod.GET;
    }
}
