package com.zys.http.tool;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.util.ClassLoaderUtil;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiTarget;
import com.zys.http.constant.HttpEnum;
import com.zys.http.entity.HttpConfig;
import com.zys.http.entity.param.ParamProperty;
import com.zys.http.entity.tree.MethodNodeData;
import com.zys.http.entity.velocity.MethodItem;
import com.zys.http.tool.convert.ParamConvert;
import com.zys.http.ui.tree.node.MethodNode;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author zhou ys
 * @since 2023-09-21
 */
@Description("Velocity 模板工具")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VelocityTool {

    private static final VelocityEngine ENGINE = new VelocityEngine();
    @Description("环境导出模板")
    private static final String ENV_TEMPLATE_PATH = "template/env.json.vm";
    @Description("接口导出模板")
    private static final String API_TEMPLATE_PATH = "template/api.json.vm";

    @Description("环境导出文件名模板, {}-环境名")
    private static final String EXPORT_ENV_FILE_NAME = "apiTool.export.postman.env.{}.json";
    @Description("接口导出文件名模板, {}-模块名")
    private static final String EXPORT_API_FILE_NAME = "api.tool.export.postman.api.{}.json";

    static {
        Properties p = new Properties();
        p.setProperty("resource.loader", "class");
        p.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
        p.setProperty(RuntimeConstants.INPUT_ENCODING, StandardCharsets.UTF_8.name());
        ClassLoaderUtil.runWithClassLoader(ClasspathResourceLoader.class.getClassLoader(), () -> ENGINE.init(p));
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
        renderTemplate(context, ENV_TEMPLATE_PATH, EXPORT_ENV_FILE_NAME, envName, exportPath);
    }


    @Description("模块导出所有 API 接口")
    public static void exportAllModuleApi(Map<String, List<PsiClass>> moduleControllerMap, Map<PsiClass, List<MethodNode>> methodNodeMap, String exportPath) throws IOException {
        VelocityContext context = new VelocityContext();

        for (Map.Entry<String, List<PsiClass>> e : moduleControllerMap.entrySet()) {
            List<PsiClass> controllers = e.getValue();
            if (controllers.isEmpty()) {
                continue;
            }
            String moduleName = e.getKey();
            context.put("moduleName", moduleName);

            List<String> controllerItems = new ArrayList<>();

            MethodItem item;
            Map<String, List<MethodItem>> methodMap = new HashMap<>();
            for (PsiClass controller : controllers) {
                HttpEnum.ContentType contentType = PsiTool.contentTypeHeader(controller);
                String classSwagger = PsiTool.getSwaggerAnnotation(controller, "CLASS_");
                classSwagger = CharSequenceUtil.isEmpty(classSwagger) ? controller.getName() : classSwagger;
                List<MethodNode> methodNodes = methodNodeMap.get(controller);
                if (methodNodes.isEmpty()) {
                    continue;
                }
                List<MethodItem> methodItems = new ArrayList<>();

                for (MethodNode methodNode : methodNodes) {
                    item = new MethodItem();
                    MethodNodeData value = methodNode.getValue();
                    item.setUri(methodNode.getFragment());
                    // 请求方式
                    HttpEnum.HttpMethod httpMethod = value.getHttpMethod();
                    item.setMethod(httpMethod.name());

                    // 请求名字
                    NavigatablePsiElement psiElement = value.getPsiElement();
                    String methodSwagger = PsiTool.getSwaggerAnnotation((PsiTarget) psiElement, "METHOD_");
                    methodSwagger = CharSequenceUtil.isEmpty(methodSwagger) ? methodNode.getFragment() : methodSwagger;
                    item.setName(methodSwagger);

                    // 请求头类型
                    HttpEnum.ContentType type = PsiTool.contentTypeHeader((PsiMethod) psiElement);
                    type = httpMethod.equals(HttpEnum.HttpMethod.GET) ? HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED : type;
                    String finalType = Objects.isNull(type) ? contentType.getValue() : type.getValue();
                    item.setContentType(finalType);

                    // 请求参数类型
                    Map<String, ParamProperty> paramPropertyMap = ParamConvert.parsePsiMethodParams((PsiMethod) psiElement, false);
                    List<String> queryParamKey = new ArrayList<>();
                    Set<String> urlencodedKey = new HashSet<>();
                    for (Map.Entry<String, ParamProperty> entry : paramPropertyMap.entrySet()) {
                        String k = entry.getKey();
                        ParamProperty v = entry.getValue();
                        HttpEnum.ParamUsage usage = v.getParamUsage();
                        switch (usage) {
                            case URL -> {
                                if (httpMethod.equals(HttpEnum.HttpMethod.POST)) {
                                    // 将参数格式化成 username=a&password=a
                                    urlencodedKey.addAll(paramPropertyMap.keySet());
                                } else {
                                    queryParamKey.add(k);
                                }
                            }
                            case BODY -> {
                                if (Objects.isNull(type)) {
                                    if (contentType.equals(HttpEnum.ContentType.APPLICATION_JSON)) {
                                        item.setMode("raw");
                                        item.setRaw(v.getDefaultValue().toString().replace("\"", "\\\""));
                                    } else {
                                        item.setMode("urlencoded");
                                    }
                                } else {
                                    item.setMode("urlencoded");
                                }
                            }
                            default -> {
                                // 不处理
                            }
                        }
                    }

                    item.setQueryKey(queryParamKey);
                    item.setUrlencodedKey(urlencodedKey);
                    methodItems.add(item);
                }
                controllerItems.add(classSwagger);
                methodMap.put(classSwagger, methodItems);

            }
            context.put("methodMap", methodMap);
            context.put("controllerItems", controllerItems);

            renderApiTemplate(context, moduleName, exportPath);
        }
    }

    private static void renderApiTemplate(VelocityContext context, String moduleName, String exportPath) throws IOException {
        renderTemplate(context, API_TEMPLATE_PATH, EXPORT_API_FILE_NAME, moduleName, exportPath);
    }

    private static void renderTemplate(VelocityContext context, String templateFilePath, String templateExportFilename, String filename, String exportPath) throws IOException {
        // 加载模板
        Template template = ENGINE.getTemplate(templateFilePath);
        // 加载模板
        String fileName = CharSequenceUtil.format(templateExportFilename, filename);
        try (
                StringWriter sw = new StringWriter();
                FileWriter fw = new FileWriter(exportPath + "/" + fileName)
        ) {
            template.merge(context, sw);
            fw.write(sw.toString());
        }
    }
}
