package com.zys.http.tool;

import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ResourceFileUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiClassImplUtil;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.zys.http.constant.SpringEnum;
import com.zys.http.entity.tree.ModuleNodeData;
import com.zys.http.entity.tree.NodeData;
import com.zys.http.entity.tree.ProjectNodeData;
import com.zys.http.ui.tree.node.BaseNode;
import com.zys.http.ui.tree.node.ModuleNode;
import com.zys.http.ui.tree.node.ProjectNode;
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

    @Description("构建出树形结构的模块目录层级")
    public static Map<String, List<String>> buildModuleLayer(@NotNull Project project) {
        // 1 将当前所有模块名存到 Map<String, List<String/Module>> 中<当前模块名,子模块名列表>
        Map<String, List<String>> moduleClassMap = new HashMap<>();

        // 2 遍历所有的模块时, 去获取父级模块的名称
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            // 如果父级模块的名称与项目名称一致, 说明是一级模块
            String parentName = ModuleRootManager.getInstance(module).getContentRoots()[0].getParent().getName();
            if (parentName.equals("Project")) {
                continue;
            }
            String moduleName = module.getName();

            List<String> list = moduleClassMap.get(parentName);

            if (Objects.isNull(list)) {
                list = new ArrayList<>();
                list.add(moduleName);
                moduleClassMap.put(parentName, list);
            } else {
                list.add(moduleName);
            }
        }

        return moduleClassMap;
    }

    @Description("构建请求树形结构结点数据")
    public static ProjectNode buildHttpApiTreeNodeData(@NotNull Project project) {
        // TODO 线程异步处理构建数据
        String projectName = project.getName();
        ProjectNode root = new ProjectNode(new ProjectNodeData(projectName));
        childNodes(project, projectName, buildModuleLayer(project)).forEach(root::add);
        return root;
    }


    private static List<BaseNode<? extends NodeData>> childNodes(@NotNull Project project, String name, @NotNull Map<String, List<String>> modules) {
        List<String> subModuleNames = modules.get(name);
        if (subModuleNames == null) {
            return Collections.emptyList();
        }
        List<BaseNode<? extends NodeData>> moduleNodes = new ArrayList<>();
        for (String childName : subModuleNames) {
            String contextPath = getContextPath(project, Objects.requireNonNull(getModuleByName(project, childName)));
            ModuleNodeData nodeData = new ModuleNodeData(childName, contextPath);
            ModuleNode moduleNode = new ModuleNode(nodeData);
            List<BaseNode<? extends NodeData>> childrenNodes = childNodes(project, childName, modules);
            childrenNodes.forEach(moduleNode::add);
            moduleNodes.add(moduleNode);
        }
        return moduleNodes;
    }

    @Description("根据模块名获取指定模块")
    public static Module getModuleByName(Project project, String moduleName) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            if (module.getName().equals(moduleName)) {
                return module;
            }
        }
        return null;
    }

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
            if (value != null) {
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

    public static PsiFile getSpringApplicationFile(@NotNull Project project, @NotNull Module module) {
        PsiManager psiManager = PsiManager.getInstance(project);
        for (String applicationFileName : APPLICATION_FILE_NAMES) {
            VirtualFile file = ResourceFileUtil.findResourceFileInDependents(module, applicationFileName);
            if (Objects.nonNull(file)) {
                return psiManager.findFile(file);
            }
        }
        return null;
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
