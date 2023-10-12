package com.zys.http.service.topic;

import com.intellij.util.messages.Topic;
import jdk.jfr.Description;


/**
 * @author zhou ys
 * @since 2023-10-10
 */
@Description("树形结构发生变化的消息通知")
public interface RefreshTreeTopic {
    @Topic.ProjectLevel
    Topic<RefreshTreeTopic> TOPIC = Topic.create(RefreshTreeTopic.class.getName(), RefreshTreeTopic.class);

    void refresh(boolean isExpand);
}
