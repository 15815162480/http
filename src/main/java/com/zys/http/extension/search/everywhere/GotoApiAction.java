package com.zys.http.extension.search.everywhere;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.ide.actions.SearchEverywhereBaseAction;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.service.NotifyService;
import com.zys.http.extension.setting.HttpSetting;
import com.zys.http.tool.SystemTool;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author zhou ys
 * @since 2023-12-22
 */
@Description("快捷打开 SE 的 ApiTool 面板")
public class GotoApiAction extends SearchEverywhereBaseAction {
    public GotoApiAction() {
        this.getTemplatePresentation().setText(Bundle.get("http.extension.search.everywhere.short.cut.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String tabId = GotoApiSearchEverywhereContributor.class.getSimpleName();
        Project project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        if (!HttpSetting.getInstance().getEnableSearchEverywhere()) {
            return;
        }

        SearchEverywhereManager seManager = SearchEverywhereManager.getInstance(project);
        try {
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
        } catch (Exception ex) {
            NotifyService.instance(Objects.requireNonNull(project)).info(Bundle.get("http.extension.search.everywhere.no.api.msg"));
        }
    }
}
