package com.zys.http.extension.topic;

import com.intellij.util.messages.Topic;
import jdk.jfr.Description;

/**
 * @author zhou ys
 * @since 2023-12-18
 */
@Description("编辑器对话框编辑完成事件")
public interface EditorDialogOkTopic {

    @Topic.ProjectLevel
    Topic<EditorDialogOkTopic> TOPIC = Topic.create(EditorDialogOkTopic.class.getName(), EditorDialogOkTopic.class);

    void modify(String modifiedText, boolean isReplace);
    void properties(String modifiedText, boolean isHeader);
}
