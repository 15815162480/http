package com.zys.http.extension.topic;

import com.intellij.util.messages.Topic;
import com.zys.http.entity.ReqHistory;

/**
 * @author zhou ys
 * @since 2024-03-01
 */
public interface HisListChangeTopic {
    @Topic.ProjectLevel
    Topic<HisListChangeTopic> TOPIC = Topic.create(HisListChangeTopic.class.getName(), HisListChangeTopic.class);

    void save(ReqHistory config);

    void remove(Integer id);
}
