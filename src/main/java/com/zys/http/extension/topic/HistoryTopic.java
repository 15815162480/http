package com.zys.http.extension.topic;

import com.intellij.util.messages.Topic;
import com.zys.http.entity.ReqHistory;

/**
 * @author zhou ys
 * @since 2024-04-16
 */
public interface HistoryTopic {
    @Topic.ProjectLevel
    Topic<Generate> GENERATE_TOPIC = Topic.create(Generate.class.getName(), Generate.class);

    @Topic.ProjectLevel
    Topic<Change> CHANGE_TOPIC = Topic.create(Change.class.getName(), Change.class);

    interface Generate {
        void generate(ReqHistory history);
    }

    interface Change {
        void save(ReqHistory config);
    }
}
