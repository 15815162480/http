package com.zys.http.tool;

import com.intellij.openapi.project.Project;
import com.zys.http.constant.HttpEnum;
import com.zys.http.entity.HttpConfig;
import com.zys.http.service.HttpService;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author zys
 * @since 2023-08-16
 */
@Description("http 配置工具类")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpServiceTool {

    private static HttpService instance;

    @Getter
    private static Project project;

    private static final HttpConfig DEFAULT_HTTP_CONFIG = new HttpConfig(HttpEnum.Protocol.HTTP,
            "127.0.0.1", Collections.emptyMap());

    public static void initHttpService(@NotNull Project project) {
        HttpServiceTool.project = project;
        instance = HttpService.getInstance(project);
    }

    public static Map<String, HttpConfig> getHttpConfigs() {
        return instance.getHttpConfigs();
    }

    public static HttpConfig getHttpConfig(String key) {
        return instance.getHttpConfigs().get(key);
    }

    public static void putHttpConfig(String key, HttpConfig httpConfig) {
        instance.addHttpConfig(key, httpConfig);
    }

    public static void removeHttpConfig(String key) {
        instance.removeHttpConfig(key);
    }

    public static HttpConfig getDefaultHttpConfig() {
        HttpConfig httpConfig = getHttpConfig(instance.getSelectedEnv());
        return Objects.isNull(httpConfig) ? DEFAULT_HTTP_CONFIG : httpConfig;
    }

    public static String getSelectedEnv() {
        return instance.getSelectedEnv();
    }

    public static void setSelectedEnv(String key) {
        instance.setSelectedEnv(key);
    }
}
