package com.zys.http.constant;

import jdk.jfr.Description;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.zys.http.constant.HttpEnum.HttpMethod;
import static com.zys.http.constant.HttpEnum.ParamUsage;

/**
 * @author zys
 * @since 2023-08-19
 */
public interface JaxRsEnum {

    @Description("JaxRs 请求路径")
    String PATH_ANNO_CLASS = "javax.ws.rs.Path";

    @Getter
    @AllArgsConstructor
    enum Method {
        GET("javax.ws.rs.GET", HttpMethod.GET),
        POST("javax.ws.rs.POST", HttpMethod.POST),
        DELETE("javax.ws.rs.DELETE", HttpMethod.DELETE),
        PUT("javax.ws.rs.PUT", HttpMethod.PUT);

        private final String clazz;
        private final HttpMethod httpMethod;
    }

    @Getter
    @AllArgsConstructor
    enum Param {
        PATH_PARAM("javax.ws.rs.PathParam", ParamUsage.PATH),
        QUERY_PARAM("javax.ws.rs.QueryParam", ParamUsage.URL),
        FORM_PARAM("javax.ws.rs.FormParam", ParamUsage.URL),
        BEAN_PARAM("javax.ws.rs.BeanParam", ParamUsage.URL),
        HEADER_PARAM("javax.ws.rs.HeaderParam", ParamUsage.USELESS),
        COOKIE_PARAM("javax.ws.rs.CookieParam", ParamUsage.USELESS),
        MATRIX_PARAM("javax.ws.rs.MatrixParam", ParamUsage.USELESS);

        private final String clazz;
        private final ParamUsage paramUsage;
    }
}
