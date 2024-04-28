package com.zys.http.tool;

import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.SpringEnum;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.name.Name;
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
            KtAnnotationEntry entry = entries.stream().filter(o -> Objects.nonNull(o.getShortName())).filter(e ->
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

            List<KtExpression> expressions = getKtExpressions(attributeNames, valueArguments);
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

        private static @NotNull List<KtExpression> getKtExpressions(String @NotNull [] attributeNames, List<? extends ValueArgument> valueArguments) {
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
            return expressions;
        }

        public static String getSwaggerAnnotation(KtNamedDeclaration element, HttpEnum.AnnotationPlace place) {
            if (Objects.isNull(element)) {
                return "";
            }

            if (!(element instanceof KtClass) && !(element instanceof KtNamedFunction)) {
                return "";
            }
            KtModifierList modifierList = element.getModifierList();
            if (Objects.isNull(modifierList)) {
                return "";
            }

            List<HttpEnum.Swagger> swagger = new ArrayList<>(List.of(HttpEnum.Swagger.values())).stream()
                    .filter(o -> o.getAnnotationPlace().equals(place))
                    .toList();

            List<KtAnnotationEntry> entries = modifierList.getAnnotationEntries();
            KtAnnotationEntry entry = entries.stream()
                    .filter(e -> {
                        if (Objects.isNull(e.getShortName())) {
                            return false;
                        }
                        String anno = e.getShortName().asString();
                        return swagger.stream().map(HttpEnum.Swagger::getClazz).anyMatch(o -> o.equals(anno) || o.endsWith(anno));
                    }).findFirst().orElse(null);
            if (Objects.isNull(entry)) {
                return "";
            }
            Name shortName = entry.getShortName();
            if (Objects.isNull(shortName)) {
                return "";
            }
            String anno = shortName.asString();

            HttpEnum.Swagger operation = swagger.stream().filter(o -> o.getClazz().equals(anno) || o.getClazz().endsWith(anno)).findFirst().orElse(null);
            if (Objects.isNull(operation)) {
                return "";
            }
            return getAnnotationValue(entry, new String[]{operation.getValue()});
        }
    }
}
