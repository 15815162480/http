package com.zys.http.ui.window;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.zys.http.ui.window.panel.SettingPanel;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

/**
 * @author zhou ys
 * @since 2024-04-09
 */
@Description("请求标签页面")
public class SettingTabWindow extends SimpleToolWindowPanel implements Disposable {

    public SettingTabWindow(@NotNull Project project) {
        super(true);
        setContent(new SettingPanel(project));
    }

    @Override
    public void dispose() {
        // 不处理
    }
}
