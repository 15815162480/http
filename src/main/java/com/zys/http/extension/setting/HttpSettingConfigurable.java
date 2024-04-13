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
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.service.NotifyService;
import com.zys.http.extension.topic.EnvListChangeTopic;
import com.zys.http.extension.topic.RefreshTreeTopic;
import com.zys.http.tool.ProjectTool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Objects;

/**
 * @author zhou ys
 * @since 2024-04-12
 */
public class HttpSettingConfigurable implements Configurable {
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
        HttpSetting setting = HttpSetting.getInstance();
        boolean generateDefault = setting.getGenerateDefault();
        boolean refreshWhenVcsChange = setting.getRefreshWhenVcsChange();
        boolean enableSearchEverywhere = setting.getEnableSearchEverywhere();
        String customAnno = setting.getCustomAnno();
        boolean panelGenerateDefault = httpSettingPanel.getGenerateDefault();
        boolean panelRefreshWhenVcsChange = httpSettingPanel.getRefreshWhenVcsChange();
        boolean panelEnableSearchEverywhere = httpSettingPanel.getEnableSearchEverywhere();
        String customControllerAnnotation = httpSettingPanel.getCustomControllerAnnotation();

        return generateDefault != panelGenerateDefault || refreshWhenVcsChange != panelRefreshWhenVcsChange
               || enableSearchEverywhere != panelEnableSearchEverywhere || !Objects.equals(customAnno, customControllerAnnotation);
    }

    @Override
    public void apply() {
        boolean generateDefault = httpSettingPanel.getGenerateDefault();
        String customControllerAnnotation = httpSettingPanel.getCustomControllerAnnotation();
        HttpSetting.getInstance().setGenerateDefault(generateDefault);
        HttpSetting.getInstance().setEnableSearchEverywhere(httpSettingPanel.getEnableSearchEverywhere());
        HttpSetting.getInstance().setRefreshWhenVcsChange(httpSettingPanel.getRefreshWhenVcsChange());

        invokeGenerateDefaultEnv(generateDefault);
        invokeCustomControllerAnnotation(customControllerAnnotation);
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

    private void invokeCustomControllerAnnotation(String customAnnotation) {
        customAnnotation = customAnnotation.trim();
        @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();

        if (CharSequenceUtil.isBlank(customAnnotation)) {
            return;
        }
        if (customAnnotation.startsWith(".") || customAnnotation.endsWith(".")) {
            HttpSetting.getInstance().setCustomAnno("");
            for (Project p : openProjects) {
                NotifyService.instance(p).info(Bundle.get("http.extension.setting.custom.annotation.msg"));
            }
            return;
        }
        HttpSetting.getInstance().setCustomAnno(customAnnotation);

        for (Project p : openProjects) {
            p.getMessageBus().syncPublisher(RefreshTreeTopic.TOPIC).refresh(false);
        }
    }

}
