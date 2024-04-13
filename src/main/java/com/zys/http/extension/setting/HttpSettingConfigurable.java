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
import com.zys.http.extension.topic.EnvListChangeTopic;
import com.zys.http.extension.topic.RefreshTreeTopic;
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
    private HttpSettingPanel httpSettingPanel = new HttpSettingPanel();

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
        return generateDefault != oldGenerateDefault ||
               refreshWhenVcsChange != oldRefreshWhenVcsChange ||
               enableSearchEverywhere != oldEnableSearchEverywhere;
    }

    @Override
    public void reset() {
        httpSettingPanel.reset(oldGenerateDefault, oldRefreshWhenVcsChange, oldEnableSearchEverywhere);
    }

    @Override
    public void apply() {
        boolean generateDefault = httpSettingPanel.getGenerateDefault();
        String customControllerAnnotation = httpSettingPanel.getCustomControllerAnnotation();
        HttpSetting.getInstance().setGenerateDefault(generateDefault);
        HttpSetting.getInstance().setEnableSearchEverywhere(httpSettingPanel.getEnableSearchEverywhere());
        HttpSetting.getInstance().setRefreshWhenVcsChange(httpSettingPanel.getRefreshWhenVcsChange());
        invokeGenerateDefaultEnv(generateDefault);
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
                        if (!ProjectTool.getModuleControllers(project, module).isEmpty()) {
                            project.getMessageBus().syncPublisher(EnvListChangeTopic.TOPIC).save(name, config);
                        }
                    } else {
                        project.getMessageBus().syncPublisher(EnvListChangeTopic.TOPIC).remove(name);
                    }
                });
            }
        });
    }

    private void invokeCustomControllerAnnotation() {
        @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project p : openProjects) {
            p.getMessageBus().syncPublisher(RefreshTreeTopic.TOPIC).refresh(false);
        }
    }

}
