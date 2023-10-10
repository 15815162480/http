package com.zys.http.ui.search;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.zys.http.ui.tree.node.MethodNode;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author zhou ys
 * @since 2023-10-10
 */
@AllArgsConstructor
public class ApiSearchChooseByNameContributor implements ChooseByNameContributor {

    private final List<MethodNode> methodNodeList;
    @Override
    public String @NotNull [] getNames(Project project, boolean includeNonProjectItems) {
        return methodNodeList.stream().map(v -> v.getValue().getNodeName()).toList().toArray(new String[0]);
    }

    @Override
    public NavigationItem @NotNull [] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        return methodNodeList.stream()
                .filter(item -> item.getValue().getNodeName() != null && item.getValue().getNodeName().equals(name))
                .map(v -> new ApiSearchItem(v.getValue()))
                .toList().toArray(new NavigationItem[0]);
    }
}
