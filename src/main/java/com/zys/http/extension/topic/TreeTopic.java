package com.zys.http.extension.topic;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.util.messages.Topic;

/**
 * @author zhou ys
 * @since 2024-04-16
 */
public interface TreeTopic {
    @Topic.ProjectLevel
    Topic<Refresh> REFRESH_TOPIC = Topic.create(Refresh.class.getName(), Refresh.class);

    @Topic.ProjectLevel
    Topic<Selected> SELECTED_TOPIC = Topic.create(Selected.class.getName(), Selected.class);

    interface Refresh {
        void refresh(boolean isExpand);
    }

    interface Selected {
        void selected(NavigatablePsiElement psiMethod);
    }
}
