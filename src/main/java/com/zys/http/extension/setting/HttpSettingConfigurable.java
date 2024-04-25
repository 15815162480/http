package com.zys.http.extension.setting;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.NlsContexts;
import com.zys.http.constant.HttpConstant;
import com.zys.http.constant.HttpEnum;
import com.zys.http.entity.HttpConfig;
import com.zys.http.extension.topic.EnvironmentTopic;
import com.zys.http.extension.topic.TreeTopic;
import com.zys.http.tool.ProjectTool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

/**
 * @author zhou ys
 * @since 2024-04-12
 */
public class HttpSettingConfigurable implements Configurable {
    private final boolean oldGenerateDefault = HttpSetting.getInstance().getGenerateDefault();
    private final boolean oldRefreshWhenVcsChange = HttpSetting.getInstance().getRefreshWhenVcsChange();
    private final boolean oldEnableSearchEverywhere = HttpSetting.getInstance().getEnableSearchEverywhere();
    private final String oldCustomAnno = HttpSetting.getInstance().getCustomAnno();
    private final long oldTimeout = HttpSetting.getInstance().getTimeout();
    private final HttpSettingPanel httpSettingPanel = new HttpSettingPanel();

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return HttpConstant.PLUGIN_NAME;
    }

    @Override
    public @Nullable JComponent createComponent() {
        return httpSettingPanel;
    }

    @Override
    public boolean isModified() {
        // 更新后的值
        HttpSetting setting = HttpSetting.getInstance();
        boolean generateDefault = setting.getGenerateDefault();
        boolean refreshWhenVcsChange = setting.getRefreshWhenVcsChange();
        boolean enableSearchEverywhere = setting.getEnableSearchEverywhere();
        String customAnno = setting.getCustomAnno();
        return generateDefault != oldGenerateDefault ||
               refreshWhenVcsChange != oldRefreshWhenVcsChange ||
               enableSearchEverywhere != oldEnableSearchEverywhere ||
               !oldCustomAnno.equals(customAnno) ||
               oldTimeout != setting.getTimeout();
    }

    @Override
    public void reset() {
        httpSettingPanel.reset(oldGenerateDefault, oldRefreshWhenVcsChange, oldEnableSearchEverywhere, oldCustomAnno, oldTimeout);
    }

    @Override
    public void apply() {
        HttpSetting setting = HttpSetting.getInstance();
        String customControllerAnnotation = setting.getCustomAnno();
        invokeGenerateDefaultEnv(setting.getGenerateDefault());
        customControllerAnnotation = customControllerAnnotation.trim();
        if (CharSequenceUtil.isBlank(customControllerAnnotation)) {
            return;
        }

        invokeCustomControllerAnnotation();
        HttpSetting.getInstance().setCustomAnno(customControllerAnnotation);
    }

    private void invokeGenerateDefaultEnv(boolean generateDefault) {
        ApplicationManager.getApplication().invokeLater(() -> {
            @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
            for (Project project : openProjects) {
                // 获取项目中所有的模块
                Collection<Module> modules = ProjectTool.moduleList(project);
                modules.forEach(module -> {
                    String name = module.getName();
                    if (generateDefault) {
                        String contextPath = ProjectTool.getModuleContextPath(project, module);
                        String port = ProjectTool.getModulePort(project, module);
                        HttpConfig config = new HttpConfig();
                        config.setProtocol(HttpEnum.Protocol.HTTP);
                        config.setHostValue("127.0.0.1:" + port + contextPath);
                        if (!ProjectTool.getModuleJavaControllers(project, module).isEmpty()) {
                            project.getMessageBus().syncPublisher(EnvironmentTopic.LIST_TOPIC).save(name, config);
                        }
                    } else {
                        project.getMessageBus().syncPublisher(EnvironmentTopic.LIST_TOPIC).remove(name);
                    }
                });
            }
        });
    }

    private void invokeCustomControllerAnnotation() {
        @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project p : openProjects) {
            p.getMessageBus().syncPublisher(TreeTopic.REFRESH_TOPIC).refresh(false);
        }
    }

}
