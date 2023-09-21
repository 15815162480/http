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
import org.jetbrains.annotations.Nullable;
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

    public static Project getProject() {
        return HttpServiceTool.getProject();
    }

    @Description("获取当前项目的所有模块, 并去重")
    public static Collection<Module> moduleList() {
        Module[] modules = ModuleManager.getInstance(getProject()).getModules();
        Map<String, Module> map = Arrays.stream(modules).collect(Collectors.toMap(Module::getName, v -> v));
        return map.values();
    }

    @Description("获取根模块")
    public static Module getRootModule() {
        return findModuleByName(getProject().getName());
    }

    @Description("根据模块名查找获取模块")
    public static Module findModuleByName(@Nullable String moduleName) {
        return moduleList().stream().filter(m -> m.getName().equals(moduleName)).findFirst().orElse(null);
    }

    @Description("获取模块的 context-path")
    public static String getModuleContextPath(@NotNull Module module) {
        // 1 获取 SpringBoot 中有的配置文件
        PsiFile psiFile = getSpringApplicationFile(module);
        if (Objects.isNull(psiFile)) {
            return "";
        }
        // 如果是 yaml 文件
        if (psiFile instanceof YAMLFile yamlFile) {
            Pair<PsiElement, String> value = YAMLUtil.getValue(yamlFile, "server", "servlet", "context-path");
            if (Objects.nonNull(value)) {
                PsiElement first = value.getFirst();
                String text = first.getText();
                return text.split(":")[0].trim();
            }
        }
        if (psiFile instanceof PropertiesFile propertiesFile) {
            return propertiesFile.getNamesMap().get("server.servlet.context-path");
        }

        return "";
    }

    @Description("获取模块所有的 Controller")
    public static List<PsiClass> getModuleControllers(Module module) {
        Optional<GlobalSearchScope> globalSearchScope = Optional.of(module)
                .map(Module::getModuleScope);
        return Stream.concat(
                        globalSearchScope.map(moduleScope -> JavaAnnotationIndex.getInstance().get(SpringEnum.Controller.CONTROLLER.getShortClassName(), getProject(), moduleScope))
                                .orElse(new ArrayList<>()).stream(),
                        globalSearchScope.map(moduleScope -> JavaAnnotationIndex.getInstance().get(SpringEnum.Controller.REST_CONTROLLER.getShortClassName(), getProject(), moduleScope))
                                .orElse(new ArrayList<>()).stream())
                .map(PsiElement::getParent)
                .map(PsiModifierList.class::cast)
                .map(PsiModifierList::getParent)
                .filter(PsiClass.class::isInstance)
                .map(PsiClass.class::cast)
                .toList();
    }

    @Description("获取模块的 context-path")
    public static String getModulePort(@NotNull Module module) {
        // 1 获取 SpringBoot 中有的配置文件
        PsiFile psiFile = getSpringApplicationFile(module);
        if (Objects.isNull(psiFile)) {
            return "80";
        }
        // 如果是 yaml 文件
        if (psiFile instanceof YAMLFile yamlFile) {
            Pair<PsiElement, String> value = YAMLUtil.getValue(yamlFile, "server", "port");
            if (Objects.nonNull(value)) {
                PsiElement first = value.getFirst();
                String text = first.getText(); // 获取到 server.servlet.context-path, 内容: context-path: /
                String port = text.split(":")[0].trim();
                return CharSequenceUtil.isEmpty(port) ? "80" : port;
            }
        }
        if (psiFile instanceof PropertiesFile propertiesFile) {
            String port = propertiesFile.getNamesMap().get("server.port");
            return CharSequenceUtil.isEmpty(port) ? "80" : port;
        }

        return "80";
    }

    @Description("获取模块中的 SpringBoot 优先级最高的配置文件")
    public static PsiFile getSpringApplicationFile(@NotNull Module module) {
        PsiManager psiManager = PsiManager.getInstance(getProject());
        for (String applicationFileName : APPLICATION_FILE_NAMES) {
            VirtualFile file = ResourceFileUtil.findResourceFileInScope(applicationFileName, getProject(), module.getModuleScope());
            if (Objects.nonNull(file)) {
                return psiManager.findFile(file);
            }
        }
        return null;
    }
}
