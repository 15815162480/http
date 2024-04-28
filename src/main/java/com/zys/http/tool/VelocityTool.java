package com.zys.http.tool;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ClassLoaderUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.SpringEnum;
import com.zys.http.entity.HttpConfig;
import com.zys.http.entity.param.ParamProperty;
import com.zys.http.entity.velocity.MethodItem;
import com.zys.http.tool.convert.ParamConvert;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtModifierList;
import org.jetbrains.kotlin.psi.KtNamedFunction;

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
        VelocityContext context = new VelocityContext();
        context.put("envName", envName);
        context.put("httpConfig", httpConfig);
        context.put("protocol", httpConfig.getProtocol().name().toLowerCase());
        renderEnvTemplate(context, envName, exportPath);
    }

    @Description("导出所有环境")
    public static void exportAllEnv(Map<String, HttpConfig> httpConfigMap, String exportPath) throws IOException {
        for (Map.Entry<String, HttpConfig> e : httpConfigMap.entrySet()) {
            exportEnv(e.getKey(), e.getValue(), exportPath);
        }
    }

    private static void renderEnvTemplate(VelocityContext context, String envName, String exportPath) throws IOException {
        renderTemplate(context, ENV_TEMPLATE_PATH, EXPORT_ENV_FILE_NAME, envName, exportPath);
    }

    @Description("模块导出所有 API 接口")
    public static void exportAllModuleApi(Project project, String exportPath) throws IOException {
        VelocityContext context = new VelocityContext();
        Collection<Module> moduleList = ProjectTool.moduleList(project).stream()
                .filter(v -> (!ProjectTool.getModuleJavaControllers(project, v).isEmpty()) || (!ProjectTool.getModuleKtControllers(project, v).isEmpty()))
                .toList();

        for (Module m : moduleList) {
            // Module
            String contextPath = ProjectTool.getModuleContextPath(project, m);
            String moduleName = m.getName();
            context.put("moduleName", moduleName);

            // Controller
            List<PsiClass> controllers = ProjectTool.getModuleJavaControllers(project, m);
            List<KtClass> ktControllers = ProjectTool.getModuleKtControllers(project, m);
            List<String> controllerItems = new ArrayList<>();

            // Method
            Map<String, List<MethodItem>> methodMap = new HashMap<>();

            for (PsiClass c : controllers) {
                String classSwagger = JavaTool.Annotation.getSwaggerAnnotation(c, HttpEnum.AnnotationPlace.CLASS);
                classSwagger = CharSequenceUtil.isEmpty(classSwagger) ? c.getName() : classSwagger;
                List<MethodItem> methodItemList = createMethodItems(c, contextPath);
                if (!methodItemList.isEmpty()) {
                    controllerItems.add(classSwagger);
                    methodMap.put(classSwagger, methodItemList);
                }
            }

            for (KtClass kt : ktControllers) {
                String classSwagger = KotlinTool.Annotation.getSwaggerAnnotation(kt, HttpEnum.AnnotationPlace.CLASS);
                classSwagger = CharSequenceUtil.isEmpty(classSwagger) ? kt.getName() : classSwagger;

                List<MethodItem> methodItemList = createMethodItems(kt, contextPath);
                if (!methodItemList.isEmpty()) {
                    controllerItems.add(classSwagger);
                    methodMap.put(classSwagger, methodItemList);
                }
            }

            context.put("methodMap", methodMap);
            context.put("controllerItems", controllerItems);

            renderApiTemplate(context, moduleName, exportPath);
        }
    }

    private static List<MethodItem> createMethodItems(KtClass kt, String contextPath) {
        String controllerPath = KotlinTool.Class.getKtControllerPath(kt);
        List<KtNamedFunction> functions = kt.getDeclarations().stream().filter(KtNamedFunction.class::isInstance).map(KtNamedFunction.class::cast).toList();
        if (functions.isEmpty()) {
            return Collections.emptyList();
        }
        List<MethodItem> methodItems = new ArrayList<>();
        for (KtNamedFunction function : functions) {
            MethodItem methodItem = createMethodItem(function, contextPath, controllerPath);
            System.out.println(function.getName());
            System.out.println("methodItem = " + methodItem);
            if (Objects.nonNull(methodItem)) {
                methodItems.add(methodItem);
            }
        }
        return methodItems;
    }

    private static MethodItem createMethodItem(KtNamedFunction function, String contextPath, String controllerPath) {
        KtModifierList modifierList = function.getModifierList();
        if (Objects.isNull(modifierList)) {
            return null;
        }
        List<KtAnnotationEntry> entries = function.getAnnotationEntries();
        MethodItem item = new MethodItem();
        // 方法名
        String methodSwagger = KotlinTool.Annotation.getSwaggerAnnotation(function, HttpEnum.AnnotationPlace.METHOD);
        methodSwagger = CharSequenceUtil.isEmpty(methodSwagger) ? function.getName() : methodSwagger;
        item.setName(methodSwagger);

        // 请求方式、请求uri、请求头类型
        String path;
        HttpEnum.HttpMethod httpMethod;
        for (KtAnnotationEntry entry : entries) {
            httpMethod = SpringEnum.Method.get(Objects.requireNonNull(entry.getShortName()).asString());
            if (Objects.isNull(httpMethod)) {
                httpMethod = SpringEnum.Method.get("org.springframework.web.bind.annotation." + Objects.requireNonNull(entry.getShortName()).asString());
                if (HttpEnum.HttpMethod.REQUEST.equals(httpMethod)) {
                    httpMethod = HttpEnum.HttpMethod.requestMappingConvert(entry);
                }
            }
            if (Objects.isNull(httpMethod)) {
                continue;
            }

            item.setMethod(httpMethod.name());

            // 请求 uri
            path = KotlinTool.Annotation.getAnnotationValue(entry, new String[]{"value", "path"});
            item.setUri(UrlTool.buildMethodUri(contextPath, controllerPath, path));

            // 请求头类型

            // 请求参数类型
            buildParamProperty(function, item);


            return item;
        }

        return null;
    }

    @Description("创建 Postman API 渲染数据列表")
    private static List<MethodItem> createMethodItems(PsiClass c, String contextPath) {
        String controllerPath = JavaTool.Class.getControllerPath(c);
        PsiMethod[] methods = c.getAllMethods();
        if (methods.length < 1) {
            return Collections.emptyList();
        }
        List<MethodItem> methodItems = new ArrayList<>();
        for (PsiMethod method : methods) {
            MethodItem methodItem = createMethodItem(method, contextPath, controllerPath);
            if (Objects.nonNull(methodItem)) {
                methodItems.add(methodItem);
            }
        }
        return methodItems;
    }

    @Description("创建 Postman API 渲染数据")
    private static MethodItem createMethodItem(PsiMethod method, String contextPath, String controllerPath) {
        PsiAnnotation[] annotations = method.getAnnotations();
        MethodItem item = new MethodItem();
        // 方法名
        String methodSwagger = JavaTool.Annotation.getSwaggerAnnotation(method, HttpEnum.AnnotationPlace.METHOD);
        methodSwagger = CharSequenceUtil.isEmpty(methodSwagger) ? method.getName() : methodSwagger;
        item.setName(methodSwagger);

        // 请求方式、请求uri、请求头类型
        for (PsiAnnotation annotation : annotations) {
            HttpEnum.HttpMethod httpMethod = SpringEnum.Method.get(annotation);
            if (Objects.isNull(httpMethod)) {
                continue;
            }
            item.setMethod(httpMethod.name());

            // 请求 uri
            String path = JavaTool.Annotation.getAnnotationValue(annotation, new String[]{"value", "path"});
            item.setUri(UrlTool.buildMethodUri(contextPath, controllerPath, path));

            // 请求头类型

            // 请求参数类型
            buildParamProperty(method, item);
            return item;
        }
        return null;
    }

    @Description("处理参数类型")
    private static void buildParamProperty(PsiElement element, MethodItem item) {
        Map<String, ParamProperty> paramPropertyMap;
        if (element instanceof PsiMethod psiMethod) {
            paramPropertyMap = ParamConvert.parsePsiMethodParams(psiMethod, false);
        } else if (element instanceof KtNamedFunction function) {
            paramPropertyMap = ParamConvert.parseFunctionParams(function, false);
        } else {
            return;
        }
        HttpEnum.ContentType type = (HttpEnum.ContentType) paramPropertyMap.get(ParamConvert.REQUEST_TYPE_KEY).getDefaultValue();
        item.setContentType(type.getValue());

        List<String> queryParamKey = new ArrayList<>();
        Set<String> urlencodedKey = new HashSet<>();
        String httpMethod = item.getMethod();

        for (Map.Entry<String, ParamProperty> entry : paramPropertyMap.entrySet()) {
            String k = entry.getKey();
            ParamProperty v = entry.getValue();
            HttpEnum.ParamUsage usage = v.getParamUsage();
            switch (usage) {
                case URL -> {
                    if (httpMethod.equals(HttpEnum.HttpMethod.POST.name())) {
                        // 将参数格式化成 username=a&password=a
                        urlencodedKey.addAll(paramPropertyMap.keySet());
                    } else {
                        queryParamKey.add(k);
                    }
                }
                case BODY -> {
                    item.setMode("urlencoded");
                    if (type.equals(HttpEnum.ContentType.APPLICATION_JSON)) {
                        item.setMode("raw");
                        item.setRaw(v.getDefaultValue().toString().replace("\"", "\\\""));
                    }
                }
                default -> {
                    // 不处理
                }
            }
        }

        item.setQueryKey(queryParamKey);
        item.setUrlencodedKey(urlencodedKey);
    }

    private static void renderApiTemplate(VelocityContext context, String moduleName, String exportPath) throws IOException {
        renderTemplate(context, API_TEMPLATE_PATH, EXPORT_API_FILE_NAME, moduleName, exportPath);
    }

    private static void renderTemplate(VelocityContext context, String templateFilePath, String templateExportFilename, String filename, String exportPath) throws IOException {
        // 加载模板
        Template template = ENGINE.getTemplate(templateFilePath, StandardCharsets.UTF_8.name());
        // 加载模板
        String fileName = CharSequenceUtil.format(templateExportFilename, filename);
        try (
                StringWriter sw = new StringWriter();
                FileWriter fw = new FileWriter(exportPath + "/" + fileName, StandardCharsets.UTF_8)
        ) {
            template.merge(context, sw);
            fw.write(sw.toString());
        }
    }
}
