package com.zys.http.extension.search.everywhere;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.zys.http.entity.tree.MethodNodeData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author zhou ys
 * @since 2023-10-11
 */
public record GotoApiChooseByNameContributor(List<MethodNodeData> dataList) implements ChooseByNameContributor {
    @Override
    public String @NotNull [] getNames(Project project, boolean includeNonProjectItems) {
        return dataList.stream().map(MethodNodeData::getNodeName).sorted().toList().toArray(new String[0]);
    }

    @Override
    public NavigationItem @NotNull [] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        return dataList.stream()
                .filter(item -> item.getNodeName() != null && (item.getNodeName().equals(name) || item.getNodeName().contains(pattern)))
                .map(GotoApiItem::new).sorted()
                .toList().toArray(new NavigationItem[0]);
    }
}