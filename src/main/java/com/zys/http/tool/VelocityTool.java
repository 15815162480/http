package com.zys.http.tool;

import com.zys.http.entity.HttpConfig;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Objects;

/**
 * @author zhou ys
 * @since 2023-09-21
 */
public class VelocityTool {

    private static final String ENV_TEMPLATE_PATH = "/template/env.json.vm";
    private static final String API_TEMPLATE_PATH = "/template/env.json.vm";

    private static final String EXPORT_ENV_FILE_NAME = "api.tool.export.env.json";

    public static void exportEnv(String envName, HttpConfig httpConfig) throws IOException {
        // 创建 VelocityEngine 实例
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("resource.manager.class", "org.apache.velocity.runtime.resource.ResourceManagerImpl");
        velocityEngine.init();

        // 创建 VelocityContext
        VelocityContext context = new VelocityContext();
        context.put("name", "World");

        // 加载模板
        Template template  = velocityEngine.getTemplate(Objects.requireNonNull(VelocityTool.class.getResource(ENV_TEMPLATE_PATH)).getPath());

        // 渲染模板
        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        // 输出结果
        System.out.println(writer);
    }
}
