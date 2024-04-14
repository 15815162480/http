package com.zys.http.tool;

import com.intellij.openapi.project.Project;
import com.zys.http.entity.ReqHistory;
import com.zys.http.extension.service.HistoryService;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author zhou ys
 * @since 2024-03-01
 */
public class HistoryTool {

    private final HistoryService historyService;

    private HistoryTool(@NotNull Project project) {
        historyService = HistoryService.getInstance(project);
    }

    @Contract("_ -> new")
    public static @NotNull HistoryTool getInstance(@NotNull Project project) {
        return new HistoryTool(project);
    }

    public Map<Integer, ReqHistory> getHistories() {
        return historyService.getHistories();
    }

    public ReqHistory getHistory(int id) {
        return historyService.getHistories().get(id);
    }

    public void saveHistory(ReqHistory reqHistory) {
        historyService.save(reqHistory);
    }

    public void deleteHistory(int id) {
        historyService.delete(id);
    }
}
