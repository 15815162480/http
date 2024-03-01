package com.zys.http.extension.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.zys.http.constant.HttpConstant;
import com.zys.http.entity.ReqHistory;
import jdk.jfr.Description;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhou ys
 * @since 2024-03-01
 */
@Getter
@Description("历史记录列表")
@State(name = "HttpHistory", storages = @Storage(HttpConstant.PLUGIN_HIS_FILE_NAME))
public class HistoryService implements PersistentStateComponent<HistoryService.State> {
    private State state = new State();

    private static final int MAX_SIZE = 50;

    public static HistoryService getInstance(Project project) {
        return project.getService(HistoryService.class);
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public void save(ReqHistory reqHistory) {
        int size = this.state.httpHistories.size();
        if (size < MAX_SIZE) {
            reqHistory.setId(size);
            this.state.httpHistories.put(size, reqHistory);
            return;
        }

        int point = state.point.get();
        if (point < MAX_SIZE) {
            reqHistory.setId(point);
            this.state.httpHistories.put(point, reqHistory);
            state.point.set(point + 1);
        } else {
            this.state.point.set(0);
            reqHistory.setId(0);
            this.state.httpHistories.put(0, reqHistory);
        }
    }

    public void delete(int id) {
        this.state.httpHistories.remove(id);
        this.state.point.set(0);
    }

    public Map<Integer, ReqHistory> getHistories() {
        return state.httpHistories;
    }

    @Data
    public static class State {
        private AtomicInteger point = new AtomicInteger(0);
        private Map<Integer, ReqHistory> httpHistories = new LinkedHashMap<>();
    }
}
