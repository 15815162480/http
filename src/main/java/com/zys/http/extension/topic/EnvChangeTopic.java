package com.zys.http.extension.topic;

import com.intellij.util.messages.Topic;
import jdk.jfr.Description;

/**
 * @author zhou ys
 * @since 2023-10-10
 */
@Description("切换环境事件通知")
public interface EnvChangeTopic {
    @Topic.ProjectLevel
    Topic<EnvChangeTopic> TOPIC = Topic.create(EnvChangeTopic.class.getName(), EnvChangeTopic.class);

    void change();
}
