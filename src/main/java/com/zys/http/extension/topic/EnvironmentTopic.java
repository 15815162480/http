package com.zys.http.extension.topic;

import com.intellij.util.messages.Topic;
import com.zys.http.entity.HttpConfig;

/**
 * @author zhou ys
 * @since 2024-04-16
 */
public interface EnvironmentTopic {
    @Topic.ProjectLevel
    Topic<Change> CHANGE_TOPIC = Topic.create(Change.class.getName(), Change.class);

    @Topic.ProjectLevel
    Topic<List> LIST_TOPIC = Topic.create(List.class.getName(), List.class);

    interface Change {
        void change();
    }

    interface List {
        void save(String name, HttpConfig config);

        void edit(String name, HttpConfig config);

        void remove(String name);
    }
}
