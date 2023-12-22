package com.zys.http.extension.search.everywhere;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.ide.actions.SearchEverywhereBaseAction;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.zys.http.tool.SystemTool;
import org.jetbrains.annotations.NotNull;

/**
 * @author zhou ys
 * @since 2023-12-22
 */
public class GotoApiAction extends SearchEverywhereBaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String tabId = GotoApiSearchEverywhereContributor.class.getSimpleName();
        Project project = e.getProject();
        SearchEverywhereManager seManager = SearchEverywhereManager.getInstance(project);
        if (seManager.isShown()) {
            if (tabId.equals(seManager.getSelectedTabID())) {
                seManager.toggleEverywhereFilter();
            } else {
                seManager.setSelectedTabID(tabId);
            }
        } else {
            // 获取系统剪贴板
            String content = SystemTool.getClipboardContent();
            if (CharSequenceUtil.isEmpty(content) || !content.startsWith("/")) {
                content = "/";
            }
            seManager.show(tabId, content, e);
        }
    }
}
