package com.zys.http.constant;

import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
        REQUEST, GET, POST, PUT, DELETE, PATCH
    }

    enum Protocol {
        HTTP, HTTPS
    }

    @Description("参数的请用方式")
    enum ParamUsage {
        // 不参与, 路径参数, URL参数, 请求体参数
        USELESS, PATH, URL, BODY, HEADER
    }


    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    enum Swagger {
        CLASS_API("io.swagger.annotations.Api", "tags"),
        CLASS_TAG("io.swagger.v3.oas.annotations.tags.Tag", "name"),
        METHOD_API_OPERATION("io.swagger.annotations.ApiOperation", "value"),
        METHOD_OPERATION("io.swagger.v3.oas.annotations.Operation", "summary");

        private final String clazz;
        @Description("注解上说明功能的属性")
        private final String value;
    }


    @Description("导出类型")
    enum ExportEnum {
        /**
         * 指定环境、所有环境, 所有 API
         */
        SPECIFY_ENV, ALL_ENV, API
    }
}
