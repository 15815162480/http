package com.zys.http.tool;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ResourceFileUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.zys.http.constant.SpringEnum;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLFile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhou ys
 * @since 2023-09-18
 */
@Description("项目(模块)工具类")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectTool {
    @Description("SpringBoot 项目的配置文件")
    private static final String[] APPLICATION_FILE_NAMES = {
            "bootstrap.properties", "bootstrap.yaml", "bootstrap.yml",
            "application.properties", "application.yaml", "application.yml"
    };

    private static final String CONTEXT_PATH_KEY = "server.servlet.context-path";
    private static final String PORT_KEY = "server.port";

    @Description("获取当前项目的所有模块, 并去重")
    public static Collection<Module> moduleList(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        Map<String, Module> map = Arrays.stream(modules).collect(Collectors.toMap(Module::getName, v -> v));
        return map.values();
    }

    @Description("获取模块中的 SpringBoot 优先级最高的配置文件")
    public static PsiFile getSpringApplicationFile(Project project, @NotNull Module module) {
        PsiManager psiManager = PsiManager.getInstance(project);
        for (String applicationFileName : APPLICATION_FILE_NAMES) {
            VirtualFile file = ResourceFileUtil.findResourceFileInScope(applicationFileName, project, module.getModuleScope());
            if (Objects.nonNull(file)) {
                return psiManager.findFile(file);
            }
        }
        return null;
    }

    @Description("获取模块中配置文件中指定键的值")
    private static String getModuleProperties(Project project, Module module, String key) {
        if (CharSequenceUtil.isEmpty(key)) {
            return "";
        }
        PsiFile psiFile = getSpringApplicationFile(project, module);
        if (Objects.isNull(psiFile)) {
            return "";
        }
        if (psiFile instanceof YAMLFile yamlFile) {
            Pair<PsiElement, String> value = YAMLUtil.getValue(yamlFile, key.split("\\."));
            if (Objects.nonNull(value)) {
                PsiElement first = value.getFirst();
                String text = first.getText();
                return text.split(":")[0].trim();
            }
        }
        if (psiFile instanceof PropertiesFile propertiesFile) {
            return propertiesFile.getNamesMap().getOrDefault(key, "");
        }

        return "";
    }

    @Description("获取模块的 context-path")
    public static String getModuleContextPath(Project project, @NotNull Module module) {
        return getModuleProperties(project, module, CONTEXT_PATH_KEY);
    }

    @Description("获取模块的 port")
    public static String getModulePort(Project project, @NotNull Module module) {
        String port = getModuleProperties(project, module, PORT_KEY);
        return CharSequenceUtil.isEmpty(port) ? "8080" : port;
    }

    @Description("获取模块所有的 Controller")
    public static List<PsiClass> getModuleControllers(Project project, Module module) {
        Optional<GlobalSearchScope> globalSearchScope = Optional.of(module)
                .map(Module::getModuleScope);
        return Stream.concat(
                        globalSearchScope.map(moduleScope -> JavaAnnotationIndex.getInstance().getAnnotations(SpringEnum.Controller.CONTROLLER.getShortClassName(), project, moduleScope))
                                .orElse(new ArrayList<>()).stream(),
                        globalSearchScope.map(moduleScope -> JavaAnnotationIndex.getInstance().getAnnotations(SpringEnum.Controller.REST_CONTROLLER.getShortClassName(), project, moduleScope))
                                .orElse(new ArrayList<>()).stream())
                .map(PsiElement::getParent)
                .map(PsiModifierList.class::cast)
                .map(PsiModifierList::getParent)
                .filter(PsiClass.class::isInstance)
                .map(PsiClass.class::cast)
                .toList();
    }
}
