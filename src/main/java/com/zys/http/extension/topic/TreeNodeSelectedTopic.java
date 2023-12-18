package com.zys.http.extension.topic;

import com.intellij.util.messages.Topic;
import com.zys.http.ui.tree.node.MethodNode;
import jdk.jfr.Description;

/**
 * @author zhou ys
 * @since 2023-12-18
 */
@Description("树形节点选中事件")
public interface TreeNodeSelectedTopic {
    @Topic.ProjectLevel
    Topic<TreeNodeSelectedTopic> TOPIC = Topic.create(TreeNodeSelectedTopic.class.getName(), TreeNodeSelectedTopic.class);

    void select(MethodNode methodNode);
}
