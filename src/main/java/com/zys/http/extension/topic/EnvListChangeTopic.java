package com.zys.http.extension.topic;

import com.intellij.util.messages.Topic;
import com.zys.http.entity.HttpConfig;
import jdk.jfr.Description;

/**
 * @author zhou ys
 * @since 2023-12-18
 */
@Description("环境列表添加、编辑、删除事件通知")
public interface EnvListChangeTopic {
    @Topic.ProjectLevel
    Topic<EnvListChangeTopic> TOPIC = Topic.create(EnvListChangeTopic.class.getName(), EnvListChangeTopic.class);

    void save(String name, HttpConfig config);

    void edit(String name, HttpConfig config);

    void remove(String name);
}
