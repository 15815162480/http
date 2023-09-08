package com.zys.http.tool;

import com.intellij.openapi.project.Project;
import com.zys.http.entity.HttpConfig;
import com.zys.http.service.HttpService;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author zys
 * @since 2023-08-16
 */
@Description("http 配置工具类")
public class HttpPropertyTool {

    private final HttpService httpService;

    private HttpPropertyTool(@NotNull Project project) {
        httpService = HttpService.getInstance(project);
    }

    public static HttpPropertyTool getInstance(Project project) {
        return new HttpPropertyTool(project);
    }

    public Map<String, HttpConfig> getHttpConfigs() {
        return httpService.getHttpConfigs();
    }

    public HttpConfig getHttpConfig(String key) {
        return httpService.getHttpConfigs().get(key);
    }

    public void putHttpConfig(String key, HttpConfig httpConfig) {
        httpService.addHttpConfig(key, httpConfig);
    }

    public void removeHttpConfig(String key) {
        httpService.removeHttpConfig(key);
    }

}
