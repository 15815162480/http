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

    @Description("Java 基础数据类型")
    private static final String[] JAVA_BASIC_DATA_TYPE = {
            Boolean.class.getName(), "boolean", Byte.class.getName(), "byte", Character.class.getName(), "char",
            Double.class.getName(), "double", Float.class.getName(), "float", Integer.class.getName(), "int",
            Long.class.getName(), "long", Short.class.getName(), "short", String.class.getName()
    };

    @Description("Kotlin 基础数据类型")
    private static final String[] KOTLIN_BASIC_DATA_TYPE = {
            "Boolean", "Byte", "Int", "Short", "Long", "Float", "Double", "Char", "Number", "Array", "String"
    };

    @Description("基元类型")
    private static final Map<PsiType, Object> PSI_PRIMITIVE_TYPE_OBJECT_MAP = new HashMap<>();
    @Description("包装类")
    private static final Map<String, Object> BASIC_DATA_TYPE_OBJECT_MAP = new HashMap<>();

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
        BASIC_DATA_TYPE_OBJECT_MAP.put("org.springframework.web.servlet.ModelAndView", Collections.emptyMap());
        BASIC_DATA_TYPE_OBJECT_MAP.put("org.springframework.ui.ModelMap", Collections.emptyMap());
    }

    @Description("是否是 Java 的基础类型")
    public static boolean isJavaBasicDataType(String type) {
        return isBasicDataType(type, JAVA_BASIC_DATA_TYPE);
    }

    @Description("是否是 Kotlin 的基础类型")
    public static boolean isKotlinBasicDataType(String type) {
        return isBasicDataType(type, KOTLIN_BASIC_DATA_TYPE);
    }

    private static boolean isBasicDataType(String type, @NotNull String[] types) {
        if (CharSequenceUtil.isEmpty(type)) {
            return false;
        }
        for (String t : types) {
            if (t.equals(type)) {
                return true;
            }
        }
        return false;
    }

    @Description("根据参数类型获取默认值")
    public static Object getDefaultValueOfPsiType(PsiType psiType, Project project) {
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
        Object arrayResult = processArrayType(psiType, project);
        if (Objects.nonNull(arrayResult)) {
            return arrayResult;
        }

        Object collectionsResult = processCollectionsType(psiType, project);
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
            List<PsiField> objectProperties = PsiTool.Field.getAllObjectPropertiesWithoutStaticAndPublic(psiClass);
            for (PsiField property : objectProperties) {
                result.put(property.getName(), getDefaultValueOfPsiType(property.getType(), project));
            }
            return result;
        }
        return null;
    }

    @Description("处理数组类型")
    private static Object processArrayType(PsiType psiType, Project project) {
        String canonicalText = psiType.getCanonicalText();
        if (canonicalText.contains("[]")) {
            String arrayCanonicalText = canonicalText.substring(0, canonicalText.indexOf("["));
            if (Object.class.getName().equals(arrayCanonicalText)) {
                return new Object[0];
            }
            // 是否是基元类型或对应包装类型的数组
            Object o = BASIC_DATA_TYPE_OBJECT_MAP.get(arrayCanonicalText);
            if (Objects.nonNull(o)) {
                return new Object[]{o};
            }
            PsiClassType type = PsiType.getTypeByName(arrayCanonicalText, project, GlobalSearchScope.allScope(project));
            Object defaultValue = getDefaultValueOfPsiType(type, project);
            if (Objects.isNull(defaultValue)) {
                return new Object[0];
            } else {
                return List.of(defaultValue);
            }
        }
        return null;
    }

    @Description("处理集合类型")
    private static Object processCollectionsType(PsiType psiType, Project project) {
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
            String genericsType = PsiTool.Generics.getGenericsType(psiType);
            // 如果泛型是 java.lang.Object
            if (Object.class.getName().equals(genericsType)) {
                return new Object[0];
            }
            // ? extends ?, ? super ?
            if (CharSequenceUtil.isNotEmpty(genericsType) && (genericsType.contains(" extends ") || genericsType.contains(" super "))) {
                return new Object[0];
            }
            Object o = BASIC_DATA_TYPE_OBJECT_MAP.get(genericsType);
            if (Objects.nonNull(o)) {
                // 说明是基元类型
                return new Object[]{o};
            }
            PsiClassType type = PsiType.getTypeByName(Objects.requireNonNull(genericsType), project, GlobalSearchScope.allScope(project));
            Object defaultValue = getDefaultValueOfPsiType(type, project);
            if (Objects.isNull(defaultValue)) {
                return new Object[0];
            } else {
                return List.of(defaultValue);
            }
        }
        return null;
    }

    @Description("处理日期类型")
    private static Object processDateType(PsiClass psiClass) {
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
}
