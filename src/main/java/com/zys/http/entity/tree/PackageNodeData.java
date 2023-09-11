package com.zys.http.entity.tree;

import com.zys.http.ui.icon.HttpIcons;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author zhou ys
 * @since 2023-09-08
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class PackageNodeData extends NodeData {

    private final String moduleName;

    public PackageNodeData(String nodeName, String moduleName) {
        super(nodeName);
        this.setNodeIcon(HttpIcons.PACKAGE);
        this.moduleName = moduleName;
    }
}
