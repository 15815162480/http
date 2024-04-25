package com.zys.http.tool;

import com.zys.http.constant.SpringEnum;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author zhou ys
 * @since 2024-04-24
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KotlinTool {
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Class {
        @Description("获取 Controller 上 RequestMapping 的请求路径")
        public static String getKtControllerPath(@NotNull KtClass ktClass) {
            KtModifierList modifierList = ktClass.getModifierList();
            if (Objects.isNull(modifierList)) {
                return "";
            }

            // 确保有引用 RequestMapping, 找不到解析引用的方法, 先这样吧
            boolean hasRequest = ktClass.getContainingKtFile().getImportDirectives().stream()
                    .anyMatch(i -> i.getText().equals("import " + SpringEnum.Method.REQUEST.getClazz()) ||
                               "import org.springframework.web.bind.annotation.*".equals(i.getText())
                    );

            List<KtAnnotationEntry> entries = modifierList.getAnnotationEntries();
            KtAnnotationEntry entry = entries.stream().filter(e ->
                    (hasRequest && SpringEnum.Method.REQUEST.getClazz().endsWith(Objects.requireNonNull(e.getShortName()).toString())) ||
                    SpringEnum.Method.REQUEST.getClazz().equals(e.getShortName().toString())
            ).findFirst().orElse(null);
            if (Objects.isNull(entry)) {
                return "";
            }

            return Annotation.getAnnotationValue(entry, new String[]{"value", "path"});
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Annotation {


        public static String getAnnotationValue(@NotNull KtAnnotationEntry entry, String[] attributeNames) {
            List<? extends ValueArgument> valueArguments = entry.getValueArguments();
            if (valueArguments.isEmpty()) {
                return "";
            }

            List<KtExpression> expressions = new ArrayList<>();
            for (String attributeName : attributeNames) {
                for (ValueArgument argument : valueArguments) {
                    ValueArgumentName argumentName = argument.getArgumentName();
                    KtExpression expression = argument.getArgumentExpression();
                    if (Objects.isNull(argumentName) && "value".equals(attributeName)) {
                        expressions.add(expression);
                    }
                    if (Objects.isNull(argumentName)) {
                        continue;
                    }
                    if (argumentName.getAsName().toString().equals(attributeName)) {
                        expressions.add(expression);
                    }
                }
            }
            if (expressions.isEmpty()) {
                return "";
            }
            KtExpression expression = expressions.get(0);
            if (expression instanceof KtStringTemplateExpression) {
                String text = expression.getText();
                return text.startsWith("\"") && text.endsWith("\"") ? text.substring(1, text.length() - 1) : text;
            } else if (expression instanceof KtCollectionLiteralExpression literalExpression) {
                List<KtExpression> innerExpressions = literalExpression.getInnerExpressions();
                String text = innerExpressions.get(0).getText();
                return text.startsWith("\"") && text.endsWith("\"") ? text.substring(1, text.length() - 1) : text;
            }

            return "";
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Function {



    }
}
