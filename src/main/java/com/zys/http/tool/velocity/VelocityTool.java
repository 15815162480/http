package com.zys.http.tool.velocity;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.util.ClassLoaderUtil;
import com.zys.http.entity.HttpConfig;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

/**
 * @author zhou ys
 * @since 2023-09-21
 */
@Description("Velocity 模板工具")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VelocityTool {

    private static final VelocityEngine ENGINE = new VelocityEngine();
    @Description("环境导出模板")
    private static final String ENV_TEMPLATE_PATH = "env.json.vm";
    @Description("接口导出模板")
    private static final String API_TEMPLATE_PATH = "api.json.vm";

    @Description("环境导出文件名模板")
    private static final String EXPORT_ENV_FILE_NAME = "api.tool.export.env.{}.json";
    @Description("接口导出文件名模板")
    private static final String EXPORT_API_FILE_NAME = "api.tool.export.api.json";

    static {
        Properties p = new Properties();
        p.setProperty("resource.loader", "plugin");
        p.setProperty("plugin.resource.loader.class", "com.zys.http.tool.velocity.PluginResourceLoader");
        p.setProperty(RuntimeConstants.OUTPUT_ENCODING, StandardCharsets.UTF_8.name());
        p.setProperty(RuntimeConstants.INPUT_ENCODING, StandardCharsets.UTF_8.name());
        ClassLoaderUtil.runWithClassLoader(PluginResourceLoader.class.getClassLoader(), () -> ENGINE.init(p));
    }

    @Description("导出指定环境")
    public static void exportEnv(String envName, HttpConfig httpConfig, String exportPath) throws IOException {
        // 创建 VelocityContext
        VelocityContext context = new VelocityContext();
        context.put("envName", envName);
        context.put("httpConfig", httpConfig);
        context.put("protocol", httpConfig.getProtocol().name().toLowerCase());
        renderEnvTemplate(context, envName, exportPath);
    }

    @Description("导出所有环境")
    public static void exportAllEnv(Map<String, HttpConfig> httpConfigMap, String exportPath) throws IOException {
        VelocityContext context = new VelocityContext();

        for (Map.Entry<String, HttpConfig> e : httpConfigMap.entrySet()) {
            String envName = e.getKey();
            context.put("envName", envName);
            context.put("httpConfig", e.getValue());
            context.put("protocol", e.getValue().getProtocol().name().toLowerCase());
            renderEnvTemplate(context, envName, exportPath);
        }
    }

    private static void renderEnvTemplate(VelocityContext context, String envName, String exportPath) throws IOException {
        // 加载模板
        Template template = ENGINE.getTemplate(ENV_TEMPLATE_PATH);
        // 加载模板
        String fileName = CharSequenceUtil.format(EXPORT_ENV_FILE_NAME, envName);
        try (
                StringWriter sw = new StringWriter();
                FileWriter fw = new FileWriter(exportPath + "/" + fileName);
        ) {
            template.merge(context, sw);
            fw.write(sw.toString());
        }
    }
}
