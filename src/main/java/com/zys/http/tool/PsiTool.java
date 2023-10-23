package com.zys.http.tool;

import com.intellij.psi.*;
import com.intellij.psi.PsiModifier.ModifierConstant;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.SpringEnum;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhou ys
 * @since 2023-09-07
 */
@Description("读取项目的文件 Psi 工具类")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PsiTool {

    @Description("获取当前类的请求类型")
    public static HttpEnum.ContentType contentTypeHeader(@Nullable PsiClass psiClass) {
        if (Objects.isNull(psiClass)) {
            return HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED;
        }
        PsiModifierList modifierList = psiClass.getModifierList();
        if (Objects.isNull(modifierList)) {
            return HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED;
        }

        PsiAnnotation controller = Stream.of(modifierList.getAnnotations())
                .filter(o -> SpringEnum.Controller.REST_CONTROLLER.getClazz().equals(o.getQualifiedName()))
                .findFirst().orElse(null);
        return Objects.isNull(controller) ? HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED : HttpEnum.ContentType.APPLICATION_JSON;
    }

    public static HttpEnum.ContentType contentTypeHeader(@Nullable PsiMethod psiMethod) {
        if (Objects.isNull(psiMethod)) {
            return HttpEnum.ContentType.APPLICATION_X_FORM_URLENCODED;
        }

        return psiMethod.hasAnnotation(SpringEnum.Controller.RESPONSE_BODY.getClazz()) ?
                HttpEnum.ContentType.APPLICATION_JSON : null;
    }

    @Description("是否有 static 修饰")
    public static boolean hasStaticModifier(@Nullable PsiModifierList target) {
        return hasModifier(target, PsiModifier.STATIC);
    }

    @Description("是否有 final 修饰")
    public static boolean hasFinalModifier(@Nullable PsiModifierList target) {
        return hasModifier(target, PsiModifier.FINAL);
    }

    @Description("是否有 private 修饰")
    public static boolean hasPrivateModifier(@Nullable PsiModifierList target) {
        return hasModifier(target, PsiModifier.PRIVATE);
    }

    @Description("是否有 protected 修饰")
    public static boolean hasProtectedModifier(@Nullable PsiModifierList target) {
        return hasModifier(target, PsiModifier.PROTECTED);
    }

    @Description("是否有 public 修饰")
    public static boolean hasPublicModifier(@Nullable PsiModifierList target) {
        return hasModifier(target, PsiModifier.PUBLIC);
    }

    @Description("是否有指定的修饰符")
    private static boolean hasModifier(@Nullable PsiModifierList target, @ModifierConstant @NotNull String modifier) {
        return Objects.nonNull(target) && target.hasModifierProperty(modifier);
    }

    @Description("注解操作工具类")
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Annotation {

        @Description("获取 Controller 上 RequestMapping 的请求路径")
        public static String getControllerPath(@NotNull PsiClass psiClass) {
            PsiModifierList modifierList = psiClass.getModifierList();
            if (Objects.isNull(modifierList)) {
                return "";
            }
            PsiAnnotation requestMapping = Stream.of(modifierList.getAnnotations())
                    .filter(o -> SpringEnum.Method.REQUEST.getClazz().equals(o.getQualifiedName()))
                    .findFirst().orElse(null);
            if (Objects.isNull(requestMapping)) {
                return "";
            }
            return getAnnotationValue(requestMapping, new String[]{"value", "path"});
        }

        @Description("获取指定注解上指定属性中的值")
        public static String getAnnotationValue(PsiAnnotation annotation, String[] attributeNames) {
            List<PsiAnnotationMemberValue> initializerList = new ArrayList<>();
            for (String attributeName : attributeNames) {
                PsiAnnotationMemberValue annoValue = annotation.findAttributeValue(attributeName);
                if (annoValue instanceof PsiArrayInitializerMemberValue arrayAnnoValues) {
                    PsiAnnotationMemberValue[] initializers = arrayAnnoValues.getInitializers();
                    if (initializers.length > 0) {
                        initializerList.addAll(List.of(initializers));
                    }
                } else {
                    if (annoValue != null) {
                        initializerList.add(annoValue);
                    }
                }
            }
            return initializerList.isEmpty() ? "" : initializerList.get(0).getText().replace("\"", "");
        }

        @Description("获取 Controller 上 @Api 或 @Tag/方法上的 @ApiOperation 或 @Operation")
        public static String getSwaggerAnnotation(@NotNull PsiTarget psiTarget, HttpEnum.AnnotationPlace place) {
            PsiModifierList modifierList;
            if (psiTarget instanceof PsiClass psiClass) {
                modifierList = psiClass.getModifierList();
            } else if (psiTarget instanceof PsiMethod psiMethod) {
                modifierList = psiMethod.getModifierList();
            } else {
                return "";
            }
            if (Objects.isNull(modifierList)) {
                return "";
            }
            List<HttpEnum.Swagger> swagger = new ArrayList<>(List.of(HttpEnum.Swagger.values())).stream()
                    .filter(o -> o.getAnnotationPlace().equals(place))
                    .toList();
            List<PsiAnnotation> list = Stream.of(modifierList.getAnnotations())
                    .filter(o -> swagger.stream().map(HttpEnum.Swagger::getClazz).toList().contains(o.getQualifiedName()))
                    .toList();

            if (list.isEmpty()) {
                return "";
            }
            PsiAnnotation annotation = list.get(0);
            HttpEnum.Swagger operation = swagger.stream().filter(o -> o.getClazz().equals(annotation.getQualifiedName()))
                    .findFirst().orElse(null);
            if (Objects.isNull(operation)) {
                return "";
            }
            return getAnnotationValue(annotation, new String[]{operation.getValue()});
        }
    }

    @Description("类操作工具类")
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Class {

        @Description("获取指定类的包名")
        public static @Nullable String getPackageName(@NotNull PsiClass psiClass) {
            String qualifiedName = psiClass.getQualifiedName();
            if (Objects.isNull(qualifiedName)) {
                return null;
            }

            String fileName = psiClass.getName();
            if (Objects.isNull(fileName)) {
                return null;
            }

            if (qualifiedName.endsWith(fileName)) {
                return qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
            }

            return null;
        }

        @Description("获取所有 @xxxMapping 的方法")
        public static List<PsiMethod> getAllXxxMappingMethods(@NotNull PsiClass psiClass) {
            PsiMethod[] methods = psiClass.getAllMethods();
            if (methods.length == 0) {
                return Collections.emptyList();
            }
            Map<String, HttpEnum.HttpMethod> httpMethodMap = Arrays.stream(SpringEnum.Method.values())
                    .collect(Collectors.toMap(SpringEnum.Method::getClazz, SpringEnum.Method::getHttpMethod));
            List<PsiMethod> target = new ArrayList<>();
            for (PsiMethod method : methods) {
                PsiAnnotation[] annotations = method.getAnnotations();
                for (PsiAnnotation annotation : annotations) {
                    if (httpMethodMap.containsKey(annotation.getQualifiedName())) {
                        target.add(method);
                        break;
                    }
                }
            }

            return target.stream().sorted(Comparator.comparing(PsiMethod::getName)).toList();
        }
    }

    @Description("方法操作工具类")
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Method {
        private static final String GETTER_PREFIX = "get";
        private static final String SETTER_PREFIX = "set";

        @Description("获取对象属性对应的 getter 和 setter")
        public static List<PsiMethod> getPropertyGetterAndSetter(@NotNull PsiField psiField, @NotNull List<PsiMethod> propertiesMethods) {
            if (propertiesMethods.isEmpty()) {
                return Collections.emptyList();
            }
            String fieldName = psiField.getName();
            PsiType fieldType = psiField.getType();
            String targetFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            return propertiesMethods.stream().filter(m -> m.getName().endsWith(targetFieldName))
                    .filter(m -> {
                        String methodName = m.getName();
                        int parametersCount = m.getParameterList().getParametersCount();
                        if (methodName.startsWith(GETTER_PREFIX)) {
                            // getter 应是无参且返回值类型与字段类型一致
                            return fieldType.equals(m.getReturnType()) && parametersCount == 0;
                        }

                        if (methodName.startsWith(SETTER_PREFIX) && parametersCount == 1) {
                            // setter 应是单个参数的且参数类型与字段类型一致
                            PsiParameter parameter = m.getParameterList().getParameters()[0];
                            return fieldType.equals(parameter.getType());
                        }
                        return false;
                    })
                    .toList();
        }

        @Description("方法名是否以 get 或 set 开头")
        public static boolean methodNameStartWithGetOrSet(String name) {
            return name.startsWith(GETTER_PREFIX) || name.startsWith(SETTER_PREFIX);
        }
    }

    @Description("字段操作工具类")
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Field {
        @Description("获取类中所有对象属性, 无 static 和 public 修饰(标准的 JavaBean)")
        public static List<PsiField> getAllObjectPropertiesWithoutStaticAndPublic(@NotNull PsiClass psiClass) {
            return Arrays.stream(psiClass.getAllFields())
                    .filter(field -> !hasStaticModifier(field.getModifierList()))
                    .filter(field -> !hasPublicModifier(field.getModifierList()))
                    .toList();
        }

        @Description("获取类中所有类属性, 有 static 修饰")
        public static List<PsiField> getAllClassPropertiesWithStatic(@NotNull PsiClass psiClass) {
            return Arrays.stream(psiClass.getAllFields())
                    .filter(field -> hasStaticModifier(field.getModifierList()))
                    .toList();
        }
    }

    @Description("泛型操作工具类")
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Generics {
        private static final Pattern GENERICS_PATTERN = Pattern.compile("<(.+?)>");

        @Description("获取泛型")
        public static String getGenericsType(@NotNull PsiType psiType) {
            String canonicalText = psiType.getCanonicalText();
            Matcher matcher = GENERICS_PATTERN.matcher(canonicalText);
            return matcher.find() ? matcher.group(1) : "";
        }
    }
}
