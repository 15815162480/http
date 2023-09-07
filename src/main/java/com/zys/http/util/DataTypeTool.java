package com.zys.http.util;

import cn.hutool.core.text.CharSequenceUtil;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

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
}
