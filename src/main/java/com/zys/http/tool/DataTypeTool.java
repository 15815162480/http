package com.zys.http.tool;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.kotlin.idea.stubindex.KotlinClassShortNameIndex;
import org.jetbrains.kotlin.psi.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


/**
 * @author zhou ys
 * @since 2023-09-07
 */
@Description("数据类型工具类")
@SuppressWarnings("UnstableApiUsage")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataTypeTool {

    @Description("@JsonProperty 全限名")
    private static final String JSON_PROPERTY_ANNO_FQN = "com.fasterxml.jackson.annotation.JsonProperty";

    @Description("基元类型")
    private static final Map<PsiType, Object> PSI_PRIMITIVE_TYPE_OBJECT_MAP = new HashMap<>();
    @Description("包装类")
    private static final Map<String, Object> BASIC_DATA_TYPE_OBJECT_MAP = new HashMap<>();

    private static final List<Object> EMPTY_ARRAY = Collections.emptyList();

    static {
        // 手动处理要移除的 API
        PSI_PRIMITIVE_TYPE_OBJECT_MAP.put(new PsiPrimitiveType(JvmPrimitiveTypeKind.BOOLEAN, PsiAnnotation.EMPTY_ARRAY), false);
        PSI_PRIMITIVE_TYPE_OBJECT_MAP.put(new PsiPrimitiveType(JvmPrimitiveTypeKind.BYTE, PsiAnnotation.EMPTY_ARRAY), 0);
        PSI_PRIMITIVE_TYPE_OBJECT_MAP.put(new PsiPrimitiveType(JvmPrimitiveTypeKind.SHORT, PsiAnnotation.EMPTY_ARRAY), 0);
        PSI_PRIMITIVE_TYPE_OBJECT_MAP.put(new PsiPrimitiveType(JvmPrimitiveTypeKind.INT, PsiAnnotation.EMPTY_ARRAY), 0);
        PSI_PRIMITIVE_TYPE_OBJECT_MAP.put(new PsiPrimitiveType(JvmPrimitiveTypeKind.LONG, PsiAnnotation.EMPTY_ARRAY), 0);
        PSI_PRIMITIVE_TYPE_OBJECT_MAP.put(new PsiPrimitiveType(JvmPrimitiveTypeKind.FLOAT, PsiAnnotation.EMPTY_ARRAY), 0.0);
        PSI_PRIMITIVE_TYPE_OBJECT_MAP.put(new PsiPrimitiveType(JvmPrimitiveTypeKind.DOUBLE, PsiAnnotation.EMPTY_ARRAY), 0.0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.lang.Boolean", false);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.lang.String", "");
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.lang.Byte", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.lang.Short", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.lang.Integer", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.lang.Long", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.lang.Float", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.lang.Double", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.math.BigInteger", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.math.BigDecimal", 0.0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("boolean", false);
        BASIC_DATA_TYPE_OBJECT_MAP.put("byte", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("short", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("int", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("long", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("float", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("org.springframework.web.servlet.", Collections.emptyMap());
        BASIC_DATA_TYPE_OBJECT_MAP.put("org.springframework.ui.ModelMap", Collections.emptyMap());
        BASIC_DATA_TYPE_OBJECT_MAP.put("ModelAndView", Collections.emptyMap());
        BASIC_DATA_TYPE_OBJECT_MAP.put("ModelMap", Collections.emptyMap());
        BASIC_DATA_TYPE_OBJECT_MAP.put("Boolean", false);
        BASIC_DATA_TYPE_OBJECT_MAP.put("Byte", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("Int", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("Short", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("Long", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("Float", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("Char", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("Number", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("String", "");
        BASIC_DATA_TYPE_OBJECT_MAP.put("JvmType.Object", Collections.emptyMap());
    }

    private static @Nullable Object getDefaultValueOfPsiType(Map<String, Integer> recursionMap, PsiType psiType, Project project) {
        // 基元类型
        Object value = PSI_PRIMITIVE_TYPE_OBJECT_MAP.get(psiType);
        if (Objects.nonNull(value)) {
            return value;
        }

        final String canonicalText = psiType.getCanonicalText();
        // 基元类型对应的包装类
        value = BASIC_DATA_TYPE_OBJECT_MAP.get(canonicalText);
        if (Objects.nonNull(value)) {
            return value;
        }

        if (Object.class.getName().equals(canonicalText)) {
            return Collections.emptyMap();
        }

        // 应该是基元类型和包装类才返回空数组类型,如果是类应该处理
        Object arrayResult = processArrayType(recursionMap, psiType, project);
        if (Objects.nonNull(arrayResult)) {
            return arrayResult;
        }

        Object collectionsResult = processCollectionsType(recursionMap, psiType, project);
        if (Objects.nonNull(collectionsResult)) {
            return collectionsResult;
        }

        if (psiType instanceof PsiClassReferenceType type) {
            // 处理实体类类型
            PsiClass psiClass = type.resolve();

            if (Objects.isNull(psiClass)) {
                return null;
            }
            if (psiClass.isEnum()) {
                return Arrays.stream(psiClass.getFields()).filter(PsiEnumConstant.class::isInstance)
                        .map(PsiField::getName)
                        .findFirst().orElse("");
            }
            // 对几个比较常用的类型进行特殊处理
            Object hasResult = processDateType(psiClass);
            if (Objects.nonNull(hasResult)) {
                return hasResult;
            }
            // 类所有对象属性
            Map<String, Object> result = new LinkedHashMap<>();
            List<PsiField> objectProperties = JavaTool.Field.getAllObjectPropertiesWithoutStaticAndPublic(psiClass);
            Integer orDefault = recursionMap.getOrDefault(psiType.getCanonicalText(), 0);
            if (orDefault < 2) {
                orDefault = orDefault + 1;
                recursionMap.put(psiType.getCanonicalText(), orDefault);
                for (PsiField property : objectProperties) {
                    String propertyName = property.getName();
                    // 是否有 @JsonProperty 属性
                    if (property.hasAnnotation(JSON_PROPERTY_ANNO_FQN)) {
                        propertyName = JavaTool.Annotation.getAnnotationValue(property.getAnnotation(JSON_PROPERTY_ANNO_FQN), new String[]{"value"});
                    }

                    result.put(propertyName, getDefaultValueOfPsiType(recursionMap, property.getType(), project));
                }
            }
            return result;
        }
        return null;
    }

    @Description("根据参数类型获取默认值")
    public static Object getDefaultValueOfPsiType(PsiType psiType, Project project) {
        return getDefaultValueOfPsiType(new HashMap<>(), psiType, project);
    }

    @Description("处理数组类型")
    private static @Nullable Object processArrayType(Map<String, Integer> recursionMap, @NotNull PsiType psiType, Project project) {
        String canonicalText = psiType.getCanonicalText();
        if (canonicalText.contains("[]")) {
            String arrayCanonicalText = canonicalText.substring(0, canonicalText.indexOf("["));
            if (Object.class.getName().equals(arrayCanonicalText)) {
                return EMPTY_ARRAY;
            }
            // 是否是基元类型或对应包装类型的数组
            Object o = BASIC_DATA_TYPE_OBJECT_MAP.get(arrayCanonicalText);
            if (Objects.nonNull(o)) {
                return List.of(o);
            }
            PsiClassType type = PsiType.getTypeByName(arrayCanonicalText, project, GlobalSearchScope.allScope(project));
            Object defaultValue = getDefaultValueOfPsiType(recursionMap, type, project);
            return Objects.isNull(defaultValue) ? EMPTY_ARRAY : List.of(defaultValue);
        }
        return null;
    }

    @Description("处理集合类型")
    private static @Nullable Object processCollectionsType(Map<String, Integer> recursionMap, @NotNull PsiType psiType, Project project) {
        final String canonicalText = psiType.getCanonicalText();
        if (!canonicalText.startsWith("java.util.")) {
            return null;
        }
        // Map 直接返回空 Map, 反正不知道 key
        if (canonicalText.contains("Map")) {
            return Collections.emptyMap();
        }
        // 如果是 List/Set 考虑泛型
        if (canonicalText.contains("List") || canonicalText.contains("Set")) {
            String genericsType = JavaTool.Generics.getGenericsType(psiType);
            // 如果泛型是 java.lang.Object
            if (Object.class.getName().equals(genericsType)) {
                return EMPTY_ARRAY;
            }
            // ? extends ?, ? super ?
            if (CharSequenceUtil.isNotEmpty(genericsType) && (genericsType.contains(" extends ") || genericsType.contains(" super "))) {
                return EMPTY_ARRAY;
            }
            Object o = BASIC_DATA_TYPE_OBJECT_MAP.get(genericsType);
            if (Objects.nonNull(o)) {
                // 说明是基元类型
                return List.of(o);
            }
            PsiClassType type = PsiType.getTypeByName(Objects.requireNonNull(genericsType), project, GlobalSearchScope.allScope(project));

            Object defaultValue = getDefaultValueOfPsiType(recursionMap, type, project);
            return Objects.isNull(defaultValue) ? EMPTY_ARRAY : List.of(defaultValue);
        }
        return null;
    }

    @Description("处理日期类型")
    private static @Nullable @Unmodifiable Object processDateType(PsiClass psiClass) {
        if (Objects.isNull(psiClass)) {
            return null;
        }
        String qualifiedName = psiClass.getQualifiedName();
        if (Objects.isNull(qualifiedName)) {
            return null;
        }
        if (qualifiedName.equals(Date.class.getName()) || qualifiedName.equals(LocalDateTime.class.getName())) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        }
        if (qualifiedName.equals(LocalDate.class.getName())) {
            return new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        }
        return null;
    }

    // Kotlin

    public static Object getDefaultValueOfKtParameter(KtParameter parameter, Project project) {
        return getDefaultValueOfKtParameter(new HashMap<>(), parameter, project, null);
    }

    private static @Nullable Object getDefaultValueOfKtParameter(Map<KtTypeReference, Integer> recursionMap, KtParameter parameter, Project project, KtTypeReference typeReference) {
        if (Objects.isNull(typeReference) && Objects.isNull(parameter)) {
            return null;
        }

        if (Objects.nonNull(parameter)) {
            typeReference = parameter.getTypeReference();
        }
        if (Objects.isNull(typeReference)) {
            return null;
        }

        String typeText = typeReference.getText();

        // 基元类型对应的包装类
        Object value = BASIC_DATA_TYPE_OBJECT_MAP.get(typeText);
        if (Objects.nonNull(value)) {
            KtExpression defaultValue = parameter.getDefaultValue();
            if (Objects.nonNull(defaultValue)) {
                value = defaultValue.getText();
            }
            return value;
        }

        value = processArrayOrCollectionType(recursionMap, parameter, project, typeReference);
        if (Objects.nonNull(value)) {
            return value;
        }

        value = processDateType(parameter);
        if (Objects.nonNull(value)) {
            return value;
        }

        KtUserType userType = (KtUserType) typeReference.getTypeElement();
        if (Objects.isNull(userType)) {
            return null;
        }

        GlobalSearchScope scope = typeReference.getResolveScope();
        // 应该只有一个才对, 没法获取到 fqName
        List<KtClassOrObject> elements = (List<KtClassOrObject>) KotlinClassShortNameIndex.getInstance().get(typeReference.getText(), project, scope);
        if (elements.isEmpty()) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        List<KtClass> ktClasses = elements.stream().filter(KtClass.class::isInstance).map(KtClass.class::cast).toList();
        Integer orDefault = recursionMap.getOrDefault(typeReference, 0);
        if (orDefault < 2) {
            orDefault = orDefault + 1;
            recursionMap.put(typeReference, orDefault);
            for (KtClass ktClass : ktClasses) {
                KtPrimaryConstructor constructor = ktClass.getPrimaryConstructor();
                if (Objects.isNull(constructor)) {
                    continue;
                }
                PsiElement[] children = constructor.getChildren();
                for (PsiElement child : children) {
                    KtParameterList list = (KtParameterList) child;
                    List<KtParameter> parameters = list.getParameters();
                    for (KtParameter ktParameter : parameters) {
                        result.put(ktParameter.getName(), getDefaultValueOfKtParameter(recursionMap, ktParameter, project, null));
                    }
                }
            }
        }

        return result;
    }

    private static @Nullable Object processArrayOrCollectionType(Map<KtTypeReference, Integer> recursionMap, KtParameter parameter, Project project, KtTypeReference typeReference) {
        if (Objects.isNull(parameter) && Objects.isNull(typeReference)) {
            return EMPTY_ARRAY;
        }
        if (Objects.isNull(typeReference)) {
            typeReference = parameter.getTypeReference();
        }
        String typeText = typeReference.getText();
        if (typeText.contains("Map")) {
            return Collections.emptyMap();
        }

        if (typeText.contains("Array") || typeText.contains("List") || typeText.contains("Set")) {
            String arrayCanonicalText = typeText.substring(typeText.indexOf("<") + 1, typeText.indexOf(">"));
            arrayCanonicalText = arrayCanonicalText.endsWith("?") ? arrayCanonicalText.substring(0, arrayCanonicalText.length() - 1) : arrayCanonicalText;
            if ("JvmType.Object".equals(arrayCanonicalText)) {
                return EMPTY_ARRAY;
            }
            // 是否是基元类型或对应包装类型的数组
            Object o = BASIC_DATA_TYPE_OBJECT_MAP.get(arrayCanonicalText);
            if (Objects.nonNull(o)) {
                return List.of(o);
            }
            KtUserType userType = (KtUserType) typeReference.getTypeElement();
            if (Objects.isNull(userType)) {
                return EMPTY_ARRAY;
            }
            KtTypeArgumentList list = userType.getTypeArgumentList();
            if (Objects.isNull(list)) {
                return EMPTY_ARRAY;
            }
            List<KtTypeProjection> arguments = list.getArguments();
            if (arguments.isEmpty()) {
                return EMPTY_ARRAY;
            }
            KtTypeProjection projection = arguments.get(0);
            Object defaultValue = getDefaultValueOfKtParameter(recursionMap, null, project, projection.getTypeReference());

            return Objects.isNull(defaultValue) ? EMPTY_ARRAY : List.of(defaultValue);
        }

        return null;
    }

    @Description("处理日期类型")
    private static @Nullable Object processDateType(KtParameter parameter) {
        if (Objects.isNull(parameter)) {
            return null;
        }
        KtTypeReference typeReference = parameter.getTypeReference();
        if (Objects.isNull(typeReference)) {
            return EMPTY_ARRAY;
        }
        String typeText = typeReference.getText();
        if ("Date".equals(typeText) || "LocalDateTime".equals(typeText)) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        }
        if ("LocalDate".equals(typeText)) {
            return new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        }
        return null;
    }
}
