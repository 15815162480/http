package com.zys.http.service;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import jdk.jfr.Description;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author zys
 * @since 2023-09-19
 */
@AllArgsConstructor
@Description("消息通知")
public class NotifyService {

    private final Project project;

    private static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.findRegisteredGroup("http.notify");

    public static NotifyService instance(@NotNull Project project) {
        return project.getService(NotifyService.class);
    }

    public Notification info(@NotNull String content) {
        return notify(content, NotificationType.INFORMATION);
    }

    public Notification notify(@NotNull String content, @NotNull NotificationType type) {
        final Notification notification = Objects.requireNonNull(NOTIFICATION_GROUP).createNotification(content, type);
        notification.notify(project);
        return notification;
    }
}
