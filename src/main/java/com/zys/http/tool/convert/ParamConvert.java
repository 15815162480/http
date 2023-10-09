package com.zys.http.tool.convert;

import cn.hutool.json.JSONUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.SpringEnum;
import com.zys.http.entity.param.ParamProperty;
import com.zys.http.tool.DataTypeTool;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author zys
 * @since 2023-09-16
 */
@Description("方法参数")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParamConvert {

    private static final String ANNO_VALUE = "value";
    private static final String ANNO_NAME = "name";

    public static Map<String, ParamProperty> parsePsiMethodParams(@NotNull PsiMethod psiMethod, boolean isJsonPretty) {
        PsiParameterList parameterList = psiMethod.getParameterList();
        if (parameterList.isEmpty()) {
            return Collections.emptyMap();
        }

        PsiParameter[] parameters = parameterList.getParameters();
        if (parameters.length < 1) {
            return Collections.emptyMap();
        }
        Map<String, ParamProperty> map = new HashMap<>();
        for (PsiParameter parameter : parameters) {
            parsePsiParameter(parameter, map, isJsonPretty);
        }
        return map;
    }


    private static void parsePsiParameter(PsiParameter parameter, Map<String, ParamProperty> map, boolean isJsonPretty) {
        String parameterName = parameter.getName();
        PsiType parameterType = parameter.getType();

        if (parameter.getAnnotation(SpringEnum.Param.REQUEST_HEADER.getClazz()) != null) {
            // 如果参数注解带 @RequestHeader, 默认 String, 不作特殊处理
            map.put(parameterName, new ParamProperty("", HttpEnum.ParamUsage.HEADER));
            return;
        }
        HttpEnum.ParamUsage paramUsage = HttpEnum.ParamUsage.URL;
        PsiAnnotation requestParamAnno = parameter.getAnnotation(SpringEnum.Param.REQUEST_PARAM.getClazz());
        if (Objects.nonNull(requestParamAnno)) {
            // @RequestParam 是否有 value 或 name 属性, 如果有会将请求参数名变为那个
            PsiAnnotationMemberValue attrValue = requestParamAnno.findAttributeValue(ANNO_VALUE);
            if (Objects.isNull(attrValue)) {
                attrValue = requestParamAnno.findDeclaredAttributeValue(ANNO_NAME);
            }
            if (attrValue instanceof PsiLiteralExpressionImpl pe) {
                Object value = pe.getValue();
                if (Objects.nonNull(value)) {
                    parameterName = value.toString();
                }
            }
        }

        PsiAnnotation pathVariableAnno = parameter.getAnnotation(SpringEnum.Param.PATH_VARIABLE.getClazz());
        if (Objects.nonNull(pathVariableAnno)) {
            paramUsage = HttpEnum.ParamUsage.PATH;
            // @PathVariable 是否有 value 或 name 属性, 如果有会将请求参数名变为那个
            PsiAnnotationMemberValue attrValue = pathVariableAnno.findDeclaredAttributeValue(ANNO_VALUE);
            if (Objects.isNull(attrValue)) {
                attrValue = pathVariableAnno.findDeclaredAttributeValue(ANNO_NAME);
            }
            if (attrValue instanceof PsiLiteralExpressionImpl pe) {
                Object value = pe.getValue();
                if (value != null) {
                    parameterName = value.toString();
                }
            }
        }
        Object paramDefaultTypeValue = DataTypeTool.getDefaultValueOfPsiType(parameterType, parameter.getProject());
        if (Objects.nonNull(paramDefaultTypeValue)) {
            if (paramDefaultTypeValue instanceof Map<?, ?> paramMap) {
                PsiAnnotation requestBodyAnno = parameter.getAnnotation(SpringEnum.Param.REQUEST_BODY.getClazz());
                if (Objects.nonNull(requestBodyAnno)) {
                    // 将 paramMap 转成 Json 字符串
                    String jsonStr = isJsonPretty ? JSONUtil.toJsonPrettyStr(paramMap) : JSONUtil.toJsonStr(paramMap);
                    map.put(parameterName, new ParamProperty(jsonStr, HttpEnum.ParamUsage.BODY));
                } else {
                    paramMap.forEach((k, v) -> map.put(k.toString(), new ParamProperty(v, HttpEnum.ParamUsage.URL)));
                }
            } else {
                map.put(parameterName, new ParamProperty(paramDefaultTypeValue, paramUsage));
            }
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
            Object value = entry.getValue();
            if (!sb.isEmpty()) {
                sb.append("&");
            }
            sb.append(key).append("=").append(URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
