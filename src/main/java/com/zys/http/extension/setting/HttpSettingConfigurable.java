package com.zys.http.extension.setting;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.DumbService;
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
        return !httpSettingPanel.getState().equals(HttpSetting.getInstance().getState());
    }

    @Override
    public void reset() {
        httpSettingPanel.reset();
    }

    @Override
    public void apply() {
        HttpSetting setting = HttpSetting.getInstance();
        HttpSetting.State state = httpSettingPanel.getState();
        HttpSetting.State newState = new HttpSetting.State();
        newState.setCustomAnno(state.getCustomAnno());
        newState.setEnableSearchEverywhere(state.isEnableSearchEverywhere());
        newState.setTimeout(state.getTimeout());
        newState.setGenerateDefault(state.isGenerateDefault());
        newState.setRefreshWhenVcsChange(state.isRefreshWhenVcsChange());
        if (!state.getCustomAnno().equals(setting.getCustomAnno())) {
            applySetting();
        }
        setting.loadState(newState);
        invokeGenerateDefaultEnv(setting.getGenerateDefault());
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
                        if (!ProjectTool.getModuleJavaControllers(project, module).isEmpty() || !ProjectTool.getModuleKtControllers(project, module).isEmpty()) {
                            project.getMessageBus().syncPublisher(EnvironmentTopic.LIST_TOPIC).save(name, config);
                        }
                    } else {
                        project.getMessageBus().syncPublisher(EnvironmentTopic.LIST_TOPIC).remove(name);
                    }
                });
            }
        });
    }

    private void applySetting() {
        @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project p : openProjects) {
            DumbService.getInstance(p).smartInvokeLater(() -> p.getMessageBus().syncPublisher(TreeTopic.REFRESH_TOPIC).refresh(false));
        }
    }

    @Override
    public void disposeUIResources() {
        httpSettingPanel = null;
    }
}
