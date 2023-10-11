package com.zys.http.ui.search;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.zys.http.entity.tree.MethodNodeData;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author zhou ys
 * @since 2023-10-11
 */
@AllArgsConstructor
public class GotoApiChooseByNameContributor implements ChooseByNameContributor {

    private final List<MethodNodeData> dataList;
    @Override
    public String @NotNull [] getNames(Project project, boolean includeNonProjectItems) {
        return dataList.stream().map(MethodNodeData::getNodeName).toList().toArray(new String[0]);
    }

    @Override
    public NavigationItem @NotNull [] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        return dataList.stream()
                .filter(item -> item.getNodeName() != null && item.getNodeName().equals(name))
                .map(GotoApiItem::new)
                .toList().toArray(new NavigationItem[0]);
    }
}