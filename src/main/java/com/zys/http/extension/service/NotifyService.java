package com.zys.http.extension.service;

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

    private static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.findRegisteredGroup("http.notify");
    private final Project project;

    public static NotifyService instance(@NotNull Project project) {
        return project.getService(NotifyService.class);
    }

    public void info(@NotNull String content) {
        notify(content, NotificationType.INFORMATION);
    }

    public void error(@NotNull String content) {
        notify(content, NotificationType.ERROR);
    }

    public void notify(@NotNull String content, @NotNull NotificationType type) {
        final Notification notification = Objects.requireNonNull(NOTIFICATION_GROUP).createNotification(content, type);
        notification.notify(project);
    }
}
