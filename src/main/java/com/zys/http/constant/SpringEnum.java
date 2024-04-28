package com.zys.http.constant;

import com.intellij.psi.PsiAnnotation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zys.http.constant.HttpEnum.HttpMethod;
import static com.zys.http.constant.HttpEnum.ParamUsage;

/**
 * @author zys
 * @since 2023-08-19
 */
public interface SpringEnum {
    @Getter
    @AllArgsConstructor
    enum Method {
        GET("org.springframework.web.bind.annotation.GetMapping", HttpMethod.GET),
        POST("org.springframework.web.bind.annotation.PostMapping", HttpMethod.POST),
        PUT("org.springframework.web.bind.annotation.PutMapping", HttpMethod.PUT),
        DELETE("org.springframework.web.bind.annotation.DeleteMapping", HttpMethod.DELETE),
        REQUEST("org.springframework.web.bind.annotation.RequestMapping", HttpMethod.REQUEST),
        PATCH("org.springframework.web.bind.annotation.PatchMapping", HttpMethod.PATCH);

        private static final Map<String, HttpMethod> METHOD_MAP;

        static {
            METHOD_MAP = Arrays.stream(SpringEnum.Method.values())
                    .collect(Collectors.toMap(SpringEnum.Method::getClazz, SpringEnum.Method::getHttpMethod));
        }

        private final String clazz;
        private final HttpMethod httpMethod;

        public static boolean contains(@NotNull PsiAnnotation annotation) {
            String qualifiedName = annotation.getQualifiedName();
            return METHOD_MAP.containsKey(qualifiedName);
        }

        public static boolean contains(@NotNull String name) {
            return METHOD_MAP.containsKey(name);
        }

        public static @Nullable HttpMethod get(@NotNull PsiAnnotation annotation) {
            String qualifiedName = annotation.getQualifiedName();
            if (!METHOD_MAP.containsKey(qualifiedName)) {
                return null;
            }
            HttpEnum.HttpMethod httpMethod = METHOD_MAP.get(qualifiedName);
            if (HttpEnum.HttpMethod.REQUEST.equals(httpMethod)) {
                httpMethod = HttpEnum.HttpMethod.requestMappingConvert(annotation);
            }
            return httpMethod;
        }

        public static @Nullable HttpMethod get(@NotNull String qualifiedName) {
            if (!METHOD_MAP.containsKey(qualifiedName)) {
                return null;
            }
            return METHOD_MAP.get(qualifiedName);
        }

    }

    @Getter
    @AllArgsConstructor
    enum Controller {
        CONTROLLER("org.springframework.stereotype.Controller"),
        REST_CONTROLLER("org.springframework.web.bind.annotation.RestController"),
        RESPONSE_BODY("org.springframework.web.bind.annotation.ResponseBody");

        private final String clazz;

        public String getShortClassName() {
            return clazz.substring(clazz.lastIndexOf('.') + 1);
        }
    }

    @Getter
    @AllArgsConstructor
    enum Param {
        PATH_VARIABLE("org.springframework.web.bind.annotation.PathVariable", ParamUsage.PATH),
        REQUEST_PARAM("org.springframework.web.bind.annotation.RequestParam", ParamUsage.URL),
        REQUEST_BODY("org.springframework.web.bind.annotation.RequestBody", ParamUsage.BODY),
        MATRIX_VARIABLE("org.springframework.web.bind.annotation.MatrixVariable", ParamUsage.USELESS),
        MODEL_ATTRIBUTE("org.springframework.web.bind.annotation.ModelAttribute", ParamUsage.URL),
        REQUEST_HEADER("org.springframework.web.bind.annotation.RequestHeader", ParamUsage.HEADER),
        REQUEST_PART("org.springframework.web.bind.annotation.RequestPart", ParamUsage.FILE),
        COOKIE_VALUE("org.springframework.web.bind.annotation.CookieValue", ParamUsage.USELESS),
        SESSION_ATTRIBUTE("org.springframework.web.bind.annotation.SessionAttribute", ParamUsage.USELESS),
        REQUEST_ATTRIBUTE("org.springframework.web.bind.annotation.RequestAttribute", ParamUsage.USELESS);

        private final String clazz;
        private final ParamUsage paramUsage;

        public String getShortName() {
            return this.clazz.substring(this.clazz.lastIndexOf('.') + 1);
        }
    }
}
