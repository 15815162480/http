package com.zys.http.extension.setting;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.zys.http.constant.HttpConstant;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author zhou ys
 * @since 2024-04-12
 */
@Getter
@State(name = "HttpSetting", storages = @Storage(HttpConstant.PLUGIN_SETTING_FILE_NAME))
public class HttpSetting implements PersistentStateComponent<HttpSetting.State> {
    private State state = new State();

    public static HttpSetting getInstance() {
        return ApplicationManager.getApplication().getService(HttpSetting.class);
    }

    @Override
    public @Nullable HttpSetting.State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public boolean getGenerateDefault() {
        return state.generateDefault;
    }

    public void setGenerateDefault(boolean status) {
        state.generateDefault = status;
    }

    public boolean getRefreshWhenVcsChange() {
        return state.refreshWhenVcsChange;
    }

    public void setRefreshWhenVcsChange(boolean status) {
        state.refreshWhenVcsChange = status;
    }

    public boolean getEnableSearchEverywhere() {
        return state.enableSearchEverywhere;
    }

    public void setEnableSearchEverywhere(boolean status) {
        state.enableSearchEverywhere = status;
    }

    public String getCustomAnno() {
        return state.customAnno;
    }

    public void setCustomAnno(String customAnno) {
        state.customAnno = customAnno;
    }

    @Data
    public static class State {
        private String customAnno = "";
        private boolean generateDefault = true;
        private boolean refreshWhenVcsChange = true;
        private boolean enableSearchEverywhere = true;
    }
}
