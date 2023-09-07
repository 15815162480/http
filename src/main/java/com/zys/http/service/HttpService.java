package com.zys.http.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.zys.http.entity.HttpConfig;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zys
 * @since 2023-09-03
 */
@Getter
@State(name = "Http", storages = @Storage("httpService.xml"))
public class HttpService implements PersistentStateComponent<HttpService.State> {

    private State state = new State();

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public static HttpService getInstance(Project project) {
        return project.getService(HttpService.class);
    }

    public void addHttpConfig(@NotNull String key, @NotNull HttpConfig httpConfig) {
        state.httpConfigs.put(key, httpConfig);
        if (state.httpConfigs.size() == 1) {
            state.selectedEnv = key;
        }
    }

    public void removeHttpConfig(@NotNull String key) {
        state.httpConfigs.remove(key);
    }

    public Map<String, HttpConfig> getHttpConfigs() {
        return state.httpConfigs;
    }

    public void setSelectedEnv(String key) {
        if (state.httpConfigs.containsKey(key)) {
            state.selectedEnv = key;
        }
    }

    @Data
    public static class State {
        private String selectedEnv;
        private Map<String, HttpConfig> httpConfigs = new LinkedHashMap<>();
    }
}
