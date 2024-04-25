package com.zys.http.tool.convert;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.SpringEnum;
import com.zys.http.entity.param.ParamProperty;
import com.zys.http.tool.DataTypeTool;
import com.zys.http.tool.JavaTool;
import com.zys.http.tool.KotlinTool;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author zys
 * @since 2023-09-16
 */
@Description("方法参数")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParamConvert {

    public static final String REQUEST_TYPE_KEY = "com.zys.ApiToolRequestType";
    private static final String ANNO_VALUE = "value";
    private static final String ANNO_NAME = "name";

    public static Map<String, ParamProperty> parsePsiMethodParams(@NotNull PsiMethod psiMethod, boolean isJsonPretty) {
        List<PsiParameter> parameters = JavaTool.Method.parameters(psiMethod);
        if (parameters.isEmpty()) {
            Map<String, ParamProperty> map = new HashMap<>();
            map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED, HttpEnum.ParamUsage.HEADER));
            return map;
        }
        Map<String, ParamProperty> map = new HashMap<>();
        for (PsiParameter parameter : parameters) {
            parsePsiParameter(parameter, map, isJsonPretty);
        }
        return map;
    }


    @Description("解析参数并转换")
    private static void parsePsiParameter(@NotNull PsiParameter parameter, @NotNull Map<String, ParamProperty> map, boolean isJsonPretty) {
        String parameterName = parameter.getName();
        PsiType parameterType = parameter.getType();

        if (parameter.getAnnotation(SpringEnum.Param.REQUEST_HEADER.getClazz()) != null) {
            // 如果参数注解带 @RequestHeader, 默认 String, 不作特殊处理
            ParamProperty property = map.getOrDefault(REQUEST_TYPE_KEY, null);
            if (property == null) {
                map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED, HttpEnum.ParamUsage.HEADER));
            }
            map.put(parameterName, new ParamProperty("", HttpEnum.ParamUsage.HEADER));
            return;
        }
        HttpEnum.ParamUsage paramUsage = HttpEnum.ParamUsage.URL;
        PsiAnnotation requestParamAnno = parameter.getAnnotation(SpringEnum.Param.REQUEST_PARAM.getClazz());

        if (Objects.nonNull(requestParamAnno)) {
            // @RequestParam 是否有 value 或 name 属性, 如果有会将请求参数名变为那个
            String annotationValue = JavaTool.Annotation.getAnnotationValue(requestParamAnno, new String[]{ANNO_VALUE, ANNO_NAME});
            if (CharSequenceUtil.isNotEmpty(annotationValue)) {
                parameterName = annotationValue;
            }
        }

        String canonicalText = parameterType.getCanonicalText();
        if (canonicalText.contains("org.springframework.web.multipart.MultipartFile")) {
            // 说明是文件
            PsiAnnotation requestPartAnno = parameter.getAnnotation(SpringEnum.Param.REQUEST_PART.getClazz());
            if (Objects.nonNull(requestPartAnno)) {
                // @RequestPart 是否有 value 或 name 属性, 如果有会将请求参数名变为那个
                String annotationValue = JavaTool.Annotation.getAnnotationValue(requestPartAnno, new String[]{ANNO_VALUE, ANNO_NAME});
                if (CharSequenceUtil.isNotEmpty(annotationValue)) {
                    parameterName = annotationValue;
                }
            }

            map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.MULTIPART_FORM_DATA, HttpEnum.ParamUsage.HEADER));
            map.put(parameterName, new ParamProperty("", HttpEnum.ParamUsage.FILE));
            return;
        }

        PsiAnnotation pathVariableAnno = parameter.getAnnotation(SpringEnum.Param.PATH_VARIABLE.getClazz());
        if (Objects.nonNull(pathVariableAnno)) {
            paramUsage = HttpEnum.ParamUsage.PATH;
            // @PathVariable 是否有 value 或 name 属性, 如果有会将请求参数名变为那个
            String annotationValue = JavaTool.Annotation.getAnnotationValue(pathVariableAnno, new String[]{ANNO_VALUE, ANNO_NAME});
            if (CharSequenceUtil.isNotEmpty(annotationValue)) {
                parameterName = annotationValue;
            }
        }

        Object paramDefaultTypeValue = DataTypeTool.getDefaultValueOfPsiType(parameterType, parameter.getProject());
        if (Objects.nonNull(paramDefaultTypeValue)) {
            if (paramDefaultTypeValue instanceof Map<?, ?> paramMap) {
                PsiAnnotation requestBodyAnno = parameter.getAnnotation(SpringEnum.Param.REQUEST_BODY.getClazz());
                if (Objects.nonNull(requestBodyAnno)) {
                    // 将 paramMap 转成 Json 字符串
                    String jsonStr = isJsonPretty ? JSONUtil.toJsonPrettyStr(paramMap) : JSONUtil.toJsonStr(paramMap);
                    map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.APPLICATION_JSON, HttpEnum.ParamUsage.HEADER));
                    map.put(parameterName, new ParamProperty(jsonStr, HttpEnum.ParamUsage.BODY));
                } else {
                    map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED, HttpEnum.ParamUsage.HEADER));
                    paramMap.forEach((k, v) -> map.put(k.toString(), new ParamProperty(v, HttpEnum.ParamUsage.URL)));
                }
            } else if (paramDefaultTypeValue instanceof Collection<?> || paramDefaultTypeValue instanceof Object[]) {
                PsiAnnotation requestBodyAnno = parameter.getAnnotation(SpringEnum.Param.REQUEST_BODY.getClazz());
                if (Objects.nonNull(requestBodyAnno)) {
                    map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.APPLICATION_JSON, HttpEnum.ParamUsage.HEADER));
                    String jsonStr = isJsonPretty ? JSONUtil.toJsonPrettyStr(paramDefaultTypeValue) : JSONUtil.toJsonStr(paramDefaultTypeValue);
                    map.put(parameterName, new ParamProperty(jsonStr, HttpEnum.ParamUsage.BODY));
                } else {
                    map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED, HttpEnum.ParamUsage.HEADER));
                    map.put(parameterName, new ParamProperty(paramDefaultTypeValue, paramUsage));
                }
            } else {
                map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED, HttpEnum.ParamUsage.HEADER));
                map.put(parameterName, new ParamProperty(paramDefaultTypeValue, paramUsage));
            }
        } else {
            map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED, HttpEnum.ParamUsage.HEADER));
        }
    }

    public static String buildParamPropertyUrlParameters(Map<String, ParamProperty> parameters) {
        Map<String, String> map = new HashMap<>();
        parameters.forEach((k, v) -> map.put(k, v.getDefaultValue() + ""));
        return buildUrlParameters(map);
    }

    public static String buildUrlParameters(Map<String, String> parameters) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!sb.isEmpty()) {
                sb.append("&");
            }
            sb.append(key).append("=").append(URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    public static Map<String, ParamProperty> parseFunctionParams(@NotNull KtNamedFunction function, boolean isJsonPretty) {
        List<KtParameter> parameters = function.getValueParameters();
        if (parameters.isEmpty()) {
            Map<String, ParamProperty> map = new HashMap<>();
            map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED, HttpEnum.ParamUsage.HEADER));
            return map;
        }

        Map<String, ParamProperty> map = new HashMap<>();
        for (KtParameter parameter : parameters) {
            parseKtParameter(map, parameter, isJsonPretty);
        }
        return map;
    }

    private static void parseKtParameter(Map<String, ParamProperty> map, KtParameter parameter, boolean isJsonPretty) {
        String parameterName = parameter.getName();
        List<KtAnnotationEntry> entries = parameter.getAnnotationEntries().stream().filter(entry -> Objects.nonNull(entry.getShortName())).toList();

        // region 请求头
        KtAnnotationEntry headerAnno = entries.stream().filter(entry ->
                SpringEnum.Param.REQUEST_HEADER.getClazz().equals(Objects.requireNonNull(entry.getShortName()).asString()) ||
                SpringEnum.Param.REQUEST_HEADER.getShortName().equals(entry.getShortName().asString())
        ).findFirst().orElse(null);

        if (Objects.nonNull(headerAnno)) {
            ParamProperty property = map.getOrDefault(REQUEST_TYPE_KEY, null);
            if (property == null) {
                map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED, HttpEnum.ParamUsage.HEADER));
            }
            map.put(parameterName, new ParamProperty("", HttpEnum.ParamUsage.HEADER));
            return;
        }

        // endregion

        // 参数
        HttpEnum.ParamUsage paramUsage = HttpEnum.ParamUsage.URL;
        KtAnnotationEntry paramAnno = entries.stream().filter(entry ->
                SpringEnum.Param.REQUEST_PARAM.getClazz().equals(Objects.requireNonNull(entry.getShortName()).asString()) ||
                SpringEnum.Param.REQUEST_PARAM.getShortName().equals(entry.getShortName().asString())
        ).findFirst().orElse(null);
        if (Objects.nonNull(paramAnno)) {
            String annotationValue = KotlinTool.Annotation.getAnnotationValue(paramAnno, new String[]{ANNO_VALUE, ANNO_NAME});
            if (CharSequenceUtil.isNotEmpty(annotationValue)) {
                parameterName = annotationValue;
            }

        }

        String canonicalText = Objects.requireNonNull(parameter.getTypeReference()).getText();
        if (canonicalText.contains("org.springframework.web.multipart.MultipartFile") || "MultipartFile".equals(canonicalText)) {
            // 说明是文件
            KtAnnotationEntry partAnno = entries.stream().filter(entry ->
                    SpringEnum.Param.REQUEST_PART.getClazz().equals(Objects.requireNonNull(entry.getShortName()).asString()) ||
                    SpringEnum.Param.REQUEST_PART.getShortName().equals(entry.getShortName().asString())
            ).findFirst().orElse(null);
            if (Objects.nonNull(partAnno)) {
                // @RequestPart 是否有 value 或 name 属性, 如果有会将请求参数名变为那个
                String annotationValue = KotlinTool.Annotation.getAnnotationValue(partAnno, new String[]{ANNO_VALUE, ANNO_NAME});
                if (CharSequenceUtil.isNotEmpty(annotationValue)) {
                    parameterName = annotationValue;
                }
            }

            map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.MULTIPART_FORM_DATA, HttpEnum.ParamUsage.HEADER));
            map.put(parameterName, new ParamProperty("", HttpEnum.ParamUsage.FILE));
            return;
        }

        KtAnnotationEntry pathAnno = entries.stream().filter(entry ->
                SpringEnum.Param.PATH_VARIABLE.getClazz().equals(Objects.requireNonNull(entry.getShortName()).asString()) ||
                SpringEnum.Param.PATH_VARIABLE.getShortName().equals(entry.getShortName().asString())
        ).findFirst().orElse(null);
        if (Objects.nonNull(pathAnno)) {
            String annotationValue = KotlinTool.Annotation.getAnnotationValue(pathAnno, new String[]{ANNO_VALUE, ANNO_NAME});
            if (CharSequenceUtil.isNotEmpty(annotationValue)) {
                parameterName = annotationValue;
            }
        }

        Object paramDefaultTypeValue = DataTypeTool.getDefaultValueOfKtParameter(parameter, parameter.getProject());
        if (Objects.nonNull(paramDefaultTypeValue)) {
            if (paramDefaultTypeValue instanceof Map<?, ?> paramMap) {
                KtAnnotationEntry bodyAnno = entries.stream().filter(entry ->
                        SpringEnum.Param.REQUEST_BODY.getClazz().equals(Objects.requireNonNull(entry.getShortName()).asString()) ||
                        SpringEnum.Param.REQUEST_BODY.getShortName().equals(entry.getShortName().asString())
                ).findFirst().orElse(null);
                if (Objects.nonNull(bodyAnno)) {
                    // 将 paramMap 转成 Json 字符串
                    String jsonStr = isJsonPretty ? JSONUtil.toJsonPrettyStr(paramMap) : JSONUtil.toJsonStr(paramMap);
                    map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.APPLICATION_JSON, HttpEnum.ParamUsage.HEADER));
                    map.put(parameterName, new ParamProperty(jsonStr, HttpEnum.ParamUsage.BODY));
                } else {
                    map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED, HttpEnum.ParamUsage.HEADER));
                    paramMap.forEach((k, v) -> map.put(k.toString(), new ParamProperty(v, HttpEnum.ParamUsage.URL)));
                }
            } else if (paramDefaultTypeValue instanceof Collection<?> || paramDefaultTypeValue instanceof Object[]) {
                KtAnnotationEntry bodyAnno = entries.stream().filter(entry ->
                        SpringEnum.Param.REQUEST_BODY.getClazz().equals(Objects.requireNonNull(entry.getShortName()).asString()) ||
                        SpringEnum.Param.REQUEST_BODY.getShortName().equals(entry.getShortName().asString())
                ).findFirst().orElse(null);
                if (Objects.nonNull(bodyAnno)) {
                    map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.APPLICATION_JSON, HttpEnum.ParamUsage.HEADER));
                    String jsonStr = isJsonPretty ? JSONUtil.toJsonPrettyStr(paramDefaultTypeValue) : JSONUtil.toJsonStr(paramDefaultTypeValue);
                    map.put(parameterName, new ParamProperty(jsonStr, HttpEnum.ParamUsage.BODY));
                } else {
                    map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED, HttpEnum.ParamUsage.HEADER));
                    map.put(parameterName, new ParamProperty(paramDefaultTypeValue, paramUsage));
                }
            } else {
                map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED, HttpEnum.ParamUsage.HEADER));
                map.put(parameterName, new ParamProperty(paramDefaultTypeValue, paramUsage));
            }
        } else {
            map.put(REQUEST_TYPE_KEY, new ParamProperty(HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED, HttpEnum.ParamUsage.HEADER));
        }
    }
}
