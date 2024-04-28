package com.zys.http.constant;

import com.intellij.psi.PsiAnnotation;
import com.zys.http.tool.JavaTool;
import com.zys.http.tool.KotlinTool;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;

/**
 * @author zys
 * @since 2023-08-19
 */
public interface HttpEnum {

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    enum ContentType {
        TEXT_XML("text/xml"),
        TEXT_HTML("text/html"),
        TEXT_PLAIN("text/plain"),
        APPLICATION_XML("application/xml"),
        APPLICATION_JSON("application/json"),
        MULTIPART_FORM_DATA("multipart/form-data"),
        APPLICATION_X_FORM_URLENCODED("application/x-www-form-urlencoded");

        private final String value;

        @Override
        public String toString() {
            return value;
        }
    }

    enum HttpMethod {
        REQUEST, GET, POST, PUT, DELETE, PATCH;

        public static HttpMethod requestMappingConvert(PsiAnnotation annotation) {
            String value = JavaTool.Annotation.getAnnotationValue(annotation, new String[]{"method"});
            return requestMappingConvert(value);
        }

        public static HttpMethod requestMappingConvert(KtAnnotationEntry entry) {
            String value = KotlinTool.Annotation.getAnnotationValue(entry, new String[]{"method"});
            return requestMappingConvert(value);
        }

        @Contract(pure = true)
        private static HttpMethod requestMappingConvert(@NotNull String value) {
            return switch (value) {
                case "RequestMethod.POST" -> POST;
                case "RequestMethod.PUT" -> PUT;
                case "RequestMethod.DELETE" -> DELETE;
                case "RequestMethod.PATCH" -> PATCH;
                default -> GET;
            };
        }
    }

    enum Protocol {
        HTTP, HTTPS
    }

    enum ParamUsage {
        // 不参与, 路径参数, URL参数, 请求体参数
        USELESS, PATH, URL, BODY, HEADER, FILE
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    enum Swagger {
        API("io.swagger.annotations.Api", "tags", AnnotationPlace.CLASS),
        TAG("io.swagger.v3.oas.annotations.tags.Tag", "name", AnnotationPlace.CLASS),
        API_OPERATION("io.swagger.annotations.ApiOperation", "value", AnnotationPlace.METHOD),
        OPERATION("io.swagger.v3.oas.annotations.Operation", "summary", AnnotationPlace.METHOD);

        private final String clazz;
        @Description("注解上说明功能的属性")
        private final String value;
        private final AnnotationPlace annotationPlace;

        public @NotNull String getShortClassName() {
            return clazz.substring(clazz.lastIndexOf('.') + 1);
        }
    }


    enum ExportEnum {
        /**
         * 指定环境、所有环境, 所有 API
         */
        SPECIFY_ENV, ALL_ENV, API
    }

    enum AnnotationPlace {
        CLASS, METHOD, PARAMETER, FIELD
    }

    enum Language {
        JAVA, KOTLIN;

        @Override
        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }
}
