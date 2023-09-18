package com.zys.http.tool;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

import static com.zys.http.tool.PsiTool.FieldMethod;
import static com.zys.http.tool.PsiTool.getPsiMethods;

/**
 * @author zhou ys
 * @since 2023-09-07
 */
@Description("数据类型工具类")
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
        PSI_PRIMITIVE_TYPE_OBJECT_MAP.put(PsiType.BOOLEAN, false);
        PSI_PRIMITIVE_TYPE_OBJECT_MAP.put(PsiType.BYTE, 0);
        PSI_PRIMITIVE_TYPE_OBJECT_MAP.put(PsiType.SHORT, 0);
        PSI_PRIMITIVE_TYPE_OBJECT_MAP.put(PsiType.INT, 0);
        PSI_PRIMITIVE_TYPE_OBJECT_MAP.put(PsiType.LONG, 0);
        PSI_PRIMITIVE_TYPE_OBJECT_MAP.put(PsiType.FLOAT, 0.0);
        PSI_PRIMITIVE_TYPE_OBJECT_MAP.put(PsiType.DOUBLE, 0.0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.util.Boolean", false);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.lang.String", "");
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.lang.Byte", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.lang.Short", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.lang.Integer", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.lang.Long", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.lang.Float", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.lang.Double", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.math.BigInteger", 0);
        BASIC_DATA_TYPE_OBJECT_MAP.put("java.math.BigDecimal", 0.0);
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

    public static Object getDefaultValueOfPsiType(PsiType psiType) {
        Object value = PSI_PRIMITIVE_TYPE_OBJECT_MAP.get(psiType);
        if (Objects.nonNull(value)) {
            return value;
        }

        final String canonicalText = psiType.getCanonicalText();
        value = BASIC_DATA_TYPE_OBJECT_MAP.get(canonicalText);
        if (Objects.nonNull(value)) {
            return value;
        }

        // 数组类型
        if (canonicalText.contains("[]")) {
            return new Object[0];
        }

        if (canonicalText.startsWith("java.util.")) {
            if (canonicalText.contains("Map")) {
                return Collections.emptyMap();
            }
            if (canonicalText.contains("List")) {
                return Collections.emptyList();
            }
        }

        if (psiType instanceof PsiClassReferenceType type) {
            // Object | List<?> | Map<K, V>
            PsiClass psiClass = type.resolve();
            if (Objects.isNull(psiClass)) {
                return null;
            }
            final Object hasResult = getDefaultData(psiClass);
            if (Objects.nonNull(hasResult)) {
                return hasResult;
            }

            Map<String, Object> result = new LinkedHashMap<>();

            List<FieldMethod> fieldMethods = getPsiMethods(psiClass);
            for (FieldMethod fieldMethod : fieldMethods) {
                PsiField psiField = fieldMethod.getField();
                if (Objects.nonNull(psiField)) {
                    // 如果该 Getter|Setter 方法所对应的Field不为空
                    PsiType psiFieldType = psiField.getType();
                    if (psiFieldType.equals(psiType)) {
                        result.put(fieldMethod.getFieldName(), null);
                        continue;
                    }

                    result.put(fieldMethod.getFieldName(), getDefaultValueOfPsiType(psiFieldType));
                }
            }

            return result;
        }
        return null;
    }


    @Nullable
    public static Object getDefaultData(@Nullable PsiClass psiClass) {
        if (psiClass == null) {
            return null;
        }
        String qualifiedName = psiClass.getQualifiedName();
        if (qualifiedName == null) {
            return null;
        }
        if (qualifiedName.equals(List.class.getName())) {
            return Collections.emptyList();
        }
        if (qualifiedName.equals(Set.class.getName())) {
            return Collections.emptySet();
        }
        if (qualifiedName.equals(Map.class.getName())) {
            return Collections.emptyMap();
        }
        if (qualifiedName.equals(Date.class.getName()) || qualifiedName.equals(LocalDateTime.class.getName())) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        }
        if (qualifiedName.equals(LocalDate.class.getName())) {
            return new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        }

        final String libPackage = "java.util(.concurrent)?.[a-zA-Z0-9]*";
        @Language("RegExp") final String regList = libPackage + "List";
        @Language("RegExp") final String regSet = libPackage + "Set";
        @Language("RegExp") final String regMap = libPackage + "Map";
        if (Pattern.compile(regList).matcher(qualifiedName).find()) {
            return Collections.emptyList();
        }
        if (Pattern.compile(regSet).matcher(qualifiedName).find()) {
            return Collections.emptySet();
        }
        if (Pattern.compile(regMap).matcher(qualifiedName).find()) {
            return Collections.emptyMap();
        }

        if (Pattern.compile(libPackage).matcher(qualifiedName).find()) {
            return "NULL";
        }

        if (psiClass.getName() != null) {
            return getDefaultData(psiClass.getName());
        }

        return null;
    }

    @Nullable
    public static Object getDefaultData(@NotNull String classType) {
        switch (classType.toLowerCase(Locale.ROOT)) {
            case "string" -> {
                return "";
            }
            case "char", "character" -> {
                return 'A';
            }
            case "byte", "short", "int", "integer", "long" -> {
                return 0;
            }
            case "float", "double" -> {
                return 0.0;
            }
            case "boolean" -> {
                return true;
            }
            default -> {
                return null;
            }
        }
    }
}
