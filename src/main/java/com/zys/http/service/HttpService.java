package com.zys.http.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.zys.http.constant.HttpConstant;
import com.zys.http.entity.HttpConfig;
import jdk.jfr.Description;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author zys
 * @since 2023-09-03
 */
@Getter
@Description("缓存环境配置列表")
@State(name = "Http", storages = @Storage(HttpConstant.PLUGIN_CONFIG_FILE_NAME))
public class HttpService implements PersistentStateComponent<HttpService.State> {

    private State state = new State();

    public static HttpService getInstance(Project project) {
        return project.getService(HttpService.class);
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public void addHttpConfig(@NotNull String key, @NotNull HttpConfig httpConfig) {
        state.httpConfigs.put(key, httpConfig);
        if (state.httpConfigs.size() == 1) {
            state.selectedEnv = key;
        }
    }

    public void removeHttpConfig(@NotNull String key) {
        if (Objects.isNull(state.selectedEnv)){
            return;
        }
        if (state.selectedEnv.equals(key)) {
            state.selectedEnv = "";
        }
        state.httpConfigs.remove(key);
    }

    public Map<String, HttpConfig> getHttpConfigs() {
        return state.httpConfigs;
    }

    public String getSelectedEnv() {
        return state.selectedEnv;
    }

    public void setSelectedEnv(String key) {
        if (state.httpConfigs.containsKey(key)) {
            state.selectedEnv = key;
        }
    }

    public boolean getGenerateDefault() {
        return state.generateDefault;
    }

    public void setGenerateDefault(boolean status) {
        state.generateDefault = status;
    }

    @Data
    public static class State {
        private String selectedEnv;
        private boolean generateDefault;
        private Map<String, HttpConfig> httpConfigs = new LinkedHashMap<>();
    }
}
