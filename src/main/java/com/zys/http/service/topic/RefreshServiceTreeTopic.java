package com.zys.http.service.topic;

import com.intellij.util.messages.Topic;

/**
 * @author zhou ys
 * @since 2023-09-14
 */
public interface RefreshServiceTreeTopic {
    Topic<RefreshServiceTreeTopic> TOPIC = Topic.create("HttpTopic-Refresh", RefreshServiceTreeTopic.class);

    void refresh();
}
