package com.zys.http.tool;

import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ResourceFileUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiClassImplUtil;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.SpringEnum;
import com.zys.http.entity.tree.MethodNodeData;
import com.zys.http.ui.tree.node.MethodNode;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLFile;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhou ys
 * @since 2023-09-07
 */
@Description("读取项目的文件 Psi 工具类")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PsiTool {

    private static final String GETTER_PREFIX = "get";
    private static final String SETTER_PREFIX = "set";

    @Description("SpringBoot 项目的配置文件")
    private static final String[] APPLICATION_FILE_NAMES = {
            "bootstrap.properties", "bootstrap.yml", "application.properties", "application.yml"
    };


    // ====================================模块==========================================

    @Description("获取指定模块中所有的 Controller")
    public static List<PsiClass> getModuleController(Project project, Module module) {
        Optional<GlobalSearchScope> globalSearchScope = Optional.of(module)
                .map(Module::getModuleScope);
        return Stream.concat(
                        globalSearchScope.map(moduleScope -> JavaAnnotationIndex.getInstance().get(SpringEnum.Controller.CONTROLLER.getShortClassName(), project, moduleScope))
                                .orElse(new ArrayList<>()).stream(),
                        globalSearchScope.map(moduleScope -> JavaAnnotationIndex.getInstance().get(SpringEnum.Controller.REST_CONTROLLER.getShortClassName(), project, moduleScope))
                                .orElse(new ArrayList<>()).stream())
                .map(PsiElement::getParent)
                .map(PsiModifierList.class::cast)
                .map(PsiModifierList::getParent)
                .filter(PsiClass.class::isInstance)
                .map(PsiClass.class::cast)
                .toList();

    }

    @Description("获取模块的 context-path")
    public static String getContextPath(@NotNull Project project, @NotNull Module module) {
        // 1 获取 SpringBoot 中有的配置文件
        PsiFile psiFile = getSpringApplicationFile(project, module);
        if (Objects.isNull(psiFile)) {
            return "";
        }
        // 如果是 yaml 文件
        if (psiFile instanceof YAMLFile yamlFile) {
            Pair<PsiElement, String> value = YAMLUtil.getValue(yamlFile, "server", "servlet");
            if (Objects.nonNull(value)) {
                PsiElement first = value.getFirst();
                String text = first.getText(); // 获取到 server.servlet.context-path, 内容: context-path: /
                return text.split(":")[1].trim();
            }
        }
        if (psiFile instanceof PropertiesFile propertiesFile) {
            return propertiesFile.getNamesMap().get("server.servlet.context-path");
        }

        return "";
    }

    @Description("获取模块中的SpringBoot配置文件")
    public static PsiFile getSpringApplicationFile(@NotNull Project project, @NotNull Module module) {
        PsiManager psiManager = PsiManager.getInstance(project);
        for (String applicationFileName : APPLICATION_FILE_NAMES) {
            VirtualFile file = ResourceFileUtil.findResourceFileInScope(applicationFileName, project, module.getModuleScope());
            if (Objects.nonNull(file)) {
                return psiManager.findFile(file);
            }
        }
        return null;
    }

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

    @Description("获取 Controller 上 @Api 或 @Tag/方法上的 @ApiOperation 或 @Operation")
    public static String getSwaggerAnnotation(@NotNull PsiTarget psiTarget, String prefix) {
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
                .filter(o -> o.name().startsWith(prefix))
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

    @Description("获取 @xxxMapping 上的 value 或 path 属性")
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


    @Description("获取所有 @xxxMapping 的方法")
    public static List<MethodNode> getMappingMethods(@NotNull PsiClass psiClass, String contextPath, String controllerPath, Map<PsiMethod, MethodNode> methodNodePsiMap) {
        PsiMethod[] methods = psiClass.getAllMethods();
        if (methods.length < 1) {
            return Collections.emptyList();
        }
        Map<String, HttpEnum.HttpMethod> httpMethodMap = Arrays.stream(SpringEnum.Method.values())
                .collect(Collectors.toMap(SpringEnum.Method::getClazz, SpringEnum.Method::getHttpMethod));
        List<MethodNode> dataList = new ArrayList<>();
        MethodNode data;
        for (PsiMethod method : methods) {
            PsiAnnotation[] annotations = method.getAnnotations();
            for (PsiAnnotation annotation : annotations) {
                String qualifiedName = annotation.getQualifiedName();
                if (!httpMethodMap.containsKey(qualifiedName)) {
                    continue;
                }
                MethodNodeData data1 = buildMethodNodeData(annotation, contextPath, controllerPath, method);
                if (Objects.nonNull(data1)) {
                    data = new MethodNode(data1);
                    data1.setDescription(getSwaggerAnnotation(method, "METHOD_"));
                    dataList.add(data);
                    methodNodePsiMap.put(method, data);
                }
            }
        }
        return dataList;
    }


    private static MethodNodeData buildMethodNodeData(@NotNull PsiAnnotation annotation, String contextPath, String controllerPath, PsiMethod psiElement) {
        Map<String, HttpEnum.HttpMethod> httpMethodMap = Arrays.stream(SpringEnum.Method.values())
                .collect(Collectors.toMap(SpringEnum.Method::getClazz, SpringEnum.Method::getHttpMethod));
        String qualifiedName = annotation.getQualifiedName();
        if (!httpMethodMap.containsKey(qualifiedName)) {
            return null;
        }
        HttpEnum.HttpMethod httpMethod = httpMethodMap.get(qualifiedName);
        if (httpMethod.equals(HttpEnum.HttpMethod.REQUEST)) {
            httpMethod = HttpEnum.HttpMethod.GET;
        }
        String name = getAnnotationValue(annotation, new String[]{"value", "path"});
        MethodNodeData data = new MethodNodeData(httpMethod, name, controllerPath, contextPath);
        data.setPsiElement(psiElement);
        return data;
    }

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
            if (Objects.nonNull(fieldMethod)) {
                if (name.startsWith(GETTER_PREFIX)) {
                    fieldMethod.addFieldGetter(method);
                } else {
                    fieldMethod.addFieldSetter(method);
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    @Description("获取指定类的包名")
    public static @Nullable String getPackageName(@NotNull PsiClass psiClass) {
        String qualifiedName = psiClass.getQualifiedName();
        if (qualifiedName == null) {
            return null;
        }

        String fileName = psiClass.getName();
        if (fileName == null) {
            return null;
        }

        if (qualifiedName.endsWith(fileName)) {
            return qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
        }

        return null;
    }

    // ========================== 请求参数 ==========================================

    @Description("获取方法的所有参数")
    private static List<PsiParameter> getPsiParameterOfPsiMethod(PsiMethod psiMethod) {
        PsiParameterList parameterList = psiMethod.getParameterList();
        if (parameterList.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(parameterList.getParameters());
    }

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
        PsiModifierList modifierList = psiMethod.getModifierList();
        PsiAnnotation responseBody = Stream.of(modifierList.getAnnotations())
                .filter(o -> SpringEnum.Controller.RESPONSE_BODY.getClazz().equals(o.getQualifiedName()))
                .findFirst().orElse(null);
        return Objects.isNull(responseBody) ? null : HttpEnum.ContentType.APPLICATION_JSON;
    }

    private void getPsiMethodParameters(PsiMethod psiMethod) {
        List<PsiParameter> parameters = getPsiParameterOfPsiMethod(psiMethod);
        if (parameters.isEmpty()) {
            return;
        }
        //

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
