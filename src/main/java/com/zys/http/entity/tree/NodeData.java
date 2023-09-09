package com.zys.http.entity.tree;

import jdk.jfr.Description;
import lombok.Data;

import javax.swing.*;
import java.util.List;

/**
 * @author zhou ys
 * @since 2023-09-07
 */
@Data
public class NodeData {

    @Description("结点显示的内容")
    private String nodeName;

    @Description("结点显示的图标")
    private Icon nodeIcon;

    @Description("鼠标悬浮在结点上展示的内容")
    private String description;

    @Description("下级列表")
    private List<? extends NodeData> children;

    protected NodeData(String nodeName) {
        this.nodeName = nodeName;
    }
}
