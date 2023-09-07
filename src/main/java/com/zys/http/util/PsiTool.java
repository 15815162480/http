package com.zys.http.util;

import com.intellij.psi.*;
import com.intellij.psi.impl.PsiClassImplUtil;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhou ys
 * @since 2023-09-07
 */
@Description("读取项目的文件工具类")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PsiTool {

    private static final String GETTER_PREFIX = "get";
    private static final String SETTER_PREFIX = "set";


    public static @NotNull List<PsiClass> getAllPsiClass(@NotNull PsiClass psiClass) {
        return Optional.of(psiClass)
                .filter(c -> !c.isAnnotationType())
                .map(c -> Stream.concat(Stream.of(c), Arrays.stream(PsiClassImplUtil.getAllInnerClasses(c))
                        .filter(innerClass -> !innerClass.isAnnotationType())))
                .orElse(Stream.empty())
                .toList();
    }

    @Description("获取指定 PsiClass 中所有的字段及对应的 Getter/Setter")
    public static List<FieldMethod> getPsiMethods(@NotNull PsiClass psiClass) {
        Map<String, FieldMethod> map = Arrays.stream(psiClass.getAllFields())
                .filter(o -> !hasStaticModifier(o.getModifierList()))
                .filter(o -> !hasFinalModifier(o.getModifierList()))
                .collect(Collectors.toMap(PsiField::getName, o -> new FieldMethod(o.getName(), o)));

        List<PsiMethod> methods = Arrays.stream(psiClass.getAllMethods())
                // 过滤出长度大于 3 的方法
                .filter(o -> o.getName().length() > 3)
                // 过滤出以 get 和 set 开头的方法
                .filter(o -> methodNameStartWithGetOrSet(o.getName()))
                // 过滤出有 public 修饰的方法
                .filter(o -> hasPublicModifier(o.getModifierList()))
                // 过滤出没有 static 修饰的方法
                .filter(o -> !hasStaticModifier(o.getModifierList()))
                .toList();

        FieldMethod fieldMethod;
        for (PsiMethod method : methods) {
            String name = method.getName();
            // 获取字段名
            final String fieldName = name.substring(3, 4).toLowerCase() + name.substring(4);
            // 如果 map 中有对应的字段, 说明不为 null
            fieldMethod = map.get(fieldName);
            if (Objects.nonNull(fieldMethod)) {
                PsiField field = fieldMethod.getField();
                if (Objects.nonNull(field) && hasPublicModifier(field.getModifierList())) {
                    // 如果字段 Field 不为空且是 public 修饰则跳过检查 Getter|Setter
                    continue;
                }
            } else {
                PsiField field = psiClass.findFieldByName(fieldName, true);
                // 如果 field 不为 null, 且没有 static 和 final 修饰
                if (Objects.nonNull(field) && hasNotStaticFinalModifier(field.getModifierList())) {
                    fieldMethod = new FieldMethod(fieldName, field);
                    map.put(fieldName, fieldMethod);
                }
            }
            if (name.startsWith(GETTER_PREFIX)) {
                fieldMethod.addFieldGetter(method);
            } else {
                fieldMethod.addFieldSetter(method);
            }
        }
        return new ArrayList<>(map.values());
    }

    @Description("方法名是否以 get 或 set 开头")
    private static boolean methodNameStartWithGetOrSet(String name) {
        return name.startsWith(GETTER_PREFIX) || name.startsWith(SETTER_PREFIX);
    }

    private static boolean hasPublicModifier(@Nullable PsiModifierList target) {
        return hasModifier(target, PsiModifier.PUBLIC);
    }

    private static boolean hasPrivateModifier(@Nullable PsiModifierList target) {
        return hasModifier(target, PsiModifier.PRIVATE);
    }

    private static boolean hasStaticModifier(@Nullable PsiModifierList target) {
        return hasModifier(target, PsiModifier.STATIC);
    }

    private static boolean hasFinalModifier(@Nullable PsiModifierList target) {
        return hasModifier(target, PsiModifier.FINAL);
    }

    private static boolean hasNotStaticFinalModifier(@Nullable PsiModifierList target) {
        return !(hasStaticModifier(target) && hasFinalModifier(target));
    }

    @Description("是否有指定的修饰符")
    private static boolean hasModifier(@Nullable PsiModifierList target, @PsiModifier.ModifierConstant @NotNull String modifier) {
        return Objects.nonNull(target) && target.hasModifierProperty(modifier);
    }

    @Getter
    public static class FieldMethod {

        @Description("Getter 方法, 可能有多个")
        private final List<PsiMethod> fieldGetters = new ArrayList<>();

        @Description("Setter 方法, 可能有多个")
        private final List<PsiMethod> fieldSetters = new ArrayList<>();

        @Setter
        private String fieldName;

        @Setter
        private PsiField field;

        @Setter
        @Description("Getter 的无参方法")
        private PsiMethod noParameterMethodOfGetter;

        public FieldMethod(@NotNull String fieldName, @Nullable PsiField field) {
            this.fieldName = fieldName;
            this.field = field;
        }

        public void addFieldGetter(@NotNull PsiMethod fieldGetter) {
            this.fieldGetters.add(fieldGetter);
            if (fieldGetter.getParameterList().isEmpty()) {
                this.noParameterMethodOfGetter = fieldGetter;
            }
        }

        public void addFieldSetter(@NotNull PsiMethod fieldSetter) {
            this.fieldSetters.add(fieldSetter);
        }

        public boolean emptyGetter() {
            return fieldGetters.isEmpty();
        }

        public boolean emptySetter() {
            return fieldSetters.isEmpty();
        }
    }
}
