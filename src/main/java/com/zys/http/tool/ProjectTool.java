package com.zys.http.tool;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.zys.http.constant.SpringEnum;
import com.zys.http.entity.tree.ModuleNodeData;
import com.zys.http.entity.tree.NodeData;
import com.zys.http.entity.tree.ProjectNodeData;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author zys
 * @since 2023-09-09
 */
@Description("项目文件工具类")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectTool {
    public static Map<String, List<String>> getModules(@NotNull Project project) {
        // 1 获取当前项目名
        String projectName = project.getName();

        // 2 将当前所有模块名存到 Map<String, List<String/Module>> 中<当前模块名,子模块名列表>
        Map<String, List<String>> moduleClassMap = new HashMap<>();

        // 3 遍历所有的模块时, 去获取父级模块的名称
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {

            // 如果父级模块的名称与项目名称一致, 说明是一级模块
            String parentName = ModuleRootManager.getInstance(module).getContentRoots()[0].getParent().getName();
            if (parentName.equals("Project")) {
                continue;
            }
            // .......
            String moduleName = module.getName();

            List<String> subModuleNames = moduleClassMap.get(parentName);
            if (Objects.isNull(subModuleNames)) {
                List<String> names = new ArrayList<>();
                names.add(moduleName);
                moduleClassMap.put(parentName, names);
            } else {
                subModuleNames.add(moduleName);
            }
            if (projectName.equals(parentName)) {
                moduleClassMap.put(moduleName, new ArrayList<>());
            }
        }

        return moduleClassMap;

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


    @Description("构建请求树形结构结点数据")
    public static ProjectNodeData buildHttpApiTreeNodeData(@NotNull Project project) {
        String projectName = project.getName();
        ProjectNodeData projectData = new ProjectNodeData(projectName);
        Map<String, List<String>> modules = getModules(project);
        List<String> topModules = modules.get(projectName);
        List<NodeData> children = new ArrayList<>();
        if (Objects.isNull(topModules) || topModules.isEmpty()) {
            return projectData;
        }
        for (String topModule : topModules) {
            ModuleNodeData nodeData = new ModuleNodeData(topModule);
            nodeData.setChildren(nodeDataChildren(topModule, modules));
            children.add(nodeData);
        }
        projectData.setChildren(children);
        return projectData;
    }

    private static List<NodeData> nodeDataChildren(String name, @NotNull Map<String, List<String>> modules) {
        List<String> subModuleNames = modules.get(name);
        List<NodeData> nodeDataList = new ArrayList<>();
        for (String childName : subModuleNames) {
            ModuleNodeData nodeData = new ModuleNodeData(childName);
            List<String> list = modules.get(childName);
            if (Objects.nonNull(list) && !list.isEmpty()) {
                nodeData.setChildren(nodeDataChildren(childName, modules));
            }
            nodeDataList.add(nodeData);
        }
        return nodeDataList;
    }

}
