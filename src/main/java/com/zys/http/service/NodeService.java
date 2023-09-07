package com.zys.http.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import jdk.jfr.Description;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * @author zhou ys
 * @since 2023-09-07
 */
@Getter
@State(name = "Node", storages = @Storage("httpService.xml"))
public class NodeService implements PersistentStateComponent<NodeService.State> {

    private State state = new State();

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    @Data
    public static class State {

        @Description("是否展示模块结点")
        private Boolean showModule;

        @Description("是否展示包结点")
        private Boolean showPackage;

        @Description("是否展示控制结点")
        private Boolean showController;
    }
}
