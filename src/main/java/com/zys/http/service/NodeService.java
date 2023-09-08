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
@State(name = "NodeConfig", storages = @Storage("httpService.xml"))
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

        @Description("是否在点击后才进行扫描接口(接口较多的情况下建议开启)")
        private Boolean isLazy;
    }
}
