package com.zys.http.tool;

import com.intellij.openapi.project.Project;
import com.zys.http.constant.HttpEnum;
import com.zys.http.entity.HttpConfig;
import com.zys.http.service.HttpService;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author zys
 * @since 2023-08-16
 */
@Description("http 配置工具类")
public class HttpServiceTool {

    private static final HttpConfig DEFAULT_HTTP_CONFIG = new HttpConfig(HttpEnum.Protocol.HTTP,
            "127.0.0.1", Collections.emptyMap());
    private final HttpService httpService;

    private HttpServiceTool(@NotNull Project project) {
        httpService = HttpService.getInstance(project);
    }

    public static HttpServiceTool getInstance(@NotNull Project project) {
        return new HttpServiceTool(project);
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

    public HttpConfig getDefaultHttpConfig() {
        HttpConfig httpConfig = httpService.getHttpConfigs().get(httpService.getSelectedEnv());
        return Objects.isNull(httpConfig) ? DEFAULT_HTTP_CONFIG : httpConfig;
    }

    public String getSelectedEnv() {
        return httpService.getSelectedEnv();
    }

    public void setSelectedEnv(String key) {
        httpService.setSelectedEnv(key);
    }
}
