package com.zys.http.constant;

import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

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

        public static ContentType convert(String value) {
            return Arrays.stream(values())
                    .filter(o -> o.value.equals(value))
                    .findFirst()
                    .orElse(APPLICATION_X_FORM_URLENCODED);
        }

        @Override
        public String toString() {
            return value;
        }
    }


    enum HttpMethod {
        REQUEST, GET, POST, PUT, DELETE, PATCH;
    }

    enum Protocol {
        HTTP, HTTPS
    }

    @Description("参数的请用方式")
    enum ParamUsage {
        // 不参与, 路径参数, URL参数, 请求体参数
        USELESS, PATH, URL, BODY
    }


    @Getter
    @Description("")
    @AllArgsConstructor
    enum Swagger {
        CLASS_API("io.swagger.annotations.Api", "tags"),
        CLASS_TAG("io.swagger.v3.oas.annotations.tags.Tag", "name"),
        METHOD_API_OPERATION("io.swagger.annotations.ApiOperation", "value"),
        METHOD_OPERATION("io.swagger.v3.oas.annotations.Operation", "summary");

        private final String clazz;
        @Description("注解上说明功能的属性")
        private final String value;
    }
}
