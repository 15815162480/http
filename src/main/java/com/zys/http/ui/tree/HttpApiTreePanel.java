package com.zys.http.ui.tree;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiClass;
import com.intellij.ui.treeStructure.SimpleTree;
import com.zys.http.entity.tree.*;
import com.zys.http.tool.PsiTool;
import com.zys.http.ui.tree.node.*;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zys
 * @since 2023-09-08
 */
public class HttpApiTreePanel extends AbstractListTreePanel {

    private final transient Project project;

    private final transient Map<String, ModuleNode> moduleNodeMap = new HashMap<>();

    public HttpApiTreePanel(@NotNull Project project) {
        super(new SimpleTree());
        this.project = project;
    }

    public ModuleNode initNodes() {
        return initModuleNodes();
    }

    @Description("初始化模块结点")
    private ModuleNode initModuleNodes() {
        Module[] modules = ModuleManager.getInstance(project).getSortedModules();
        String contextPath = PsiTool.getContextPath(project, modules[0]);
        ModuleNode root = new ModuleNode(new ModuleNodeData(modules[0].getName(), contextPath));
        if (modules.length == 1) {
            moduleNodeMap.put(modules[0].getName(), root);
            initPackageNodes(root, modules[0]);
            return root;
        }

        for (Module module : modules) {
            String moduleName = module.getName();
            String parentName = ModuleRootManager.getInstance(module).getContentRoots()[0].getParent().getName();
            // 顶级模块跳过, 模块没有 Controller 类跳过
            List<PsiClass> controllers = PsiTool.getModuleController(project, module);
            if (moduleName.equals(project.getName()) || "Project".equals(parentName) || controllers.isEmpty()) {
                continue;
            }

            contextPath = PsiTool.getContextPath(project, module);
            ModuleNode moduleNode = new ModuleNode(new ModuleNodeData(moduleName, contextPath));
            moduleNodeMap.put(moduleName, moduleNode);
            ModuleNode parentNode = moduleNodeMap.get(parentName);
            if (Objects.nonNull(parentNode)) {
                parentNode.add(moduleNode);
            }
            initPackageNodes(moduleNode, module);
        }
        return root;
    }

    @Description("初始化包结点")
    private void initPackageNodes(BaseNode<? extends NodeData> parent, Module module) {
        List<PsiClass> controllers = PsiTool.getModuleController(project, module);
        if (controllers.isEmpty()) {
            return;
        }
        List<String> packageNames = controllers.stream().map(PsiClass::getQualifiedName)
                .filter(Objects::nonNull).map(o -> o.substring(0, o.lastIndexOf('.'))).toList();

        String commonPrefix = getCommonPrefix(packageNames);
        String finalCommonPrefix = commonPrefix;
        if (CharSequenceUtil.isEmpty(commonPrefix)) {
            return;
        }

        if (commonPrefix.endsWith(".")) {
            commonPrefix = commonPrefix.substring(0, commonPrefix.length() - 1);
        }
        PackageNode commonPackageNode = new PackageNode(new PackageNodeData(commonPrefix));
        // 是否有类在当前节点, 添加
        for (PsiClass controller : controllers) {
            String qualifiedName = controller.getQualifiedName();
            if (Objects.isNull(qualifiedName)) {
                continue;
            }
            qualifiedName = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
            if (qualifiedName.equals(finalCommonPrefix)) {
                initMethodNodes(controller, module, commonPackageNode);
            }
        }
        parent.add(commonPackageNode);
        // 剩余的进行分组
        Map<String, Set<String>> map = packageNames.stream().filter(o -> o.length() > finalCommonPrefix.length())
                .map(o -> o.substring(finalCommonPrefix.length()))
                .collect(Collectors.groupingBy(
                        o -> o.substring(0, o.lastIndexOf('.')),
                        Collectors.mapping(o -> o.substring(o.lastIndexOf('.') + 1), Collectors.toSet())
                ));
        map.forEach((k, v) -> {
            if (v.size() > 1) {
                PackageNode node = new PackageNode(new PackageNodeData(k));
                for (String s : v) {
                    PackageNode childPackageNode = new PackageNode(new PackageNodeData(s));
                    initClassNodes(k, s, controllers, module, childPackageNode);
                    node.add(childPackageNode);
                }
                commonPackageNode.add(node);
            } else {
                String s = v.stream().toList().get(0);
                PackageNode childPackageNode = new PackageNode(new PackageNodeData(k + "." + s));
                initClassNodes(k, s, controllers, module, childPackageNode);
                commonPackageNode.add(childPackageNode);
            }
        });
    }

    @Description("初始化类节点")
    private void initClassNodes(String package1, String package2, List<PsiClass> controllers, Module module, PackageNode parentNode) {
        for (PsiClass controller : controllers) {
            String qualifiedName = controller.getQualifiedName();
            if (Objects.isNull(qualifiedName)) {
                continue;
            }
            qualifiedName = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
            if (qualifiedName.endsWith(package1 + "." + package2)) {
                initMethodNodes(controller, module, parentNode);
            }
        }
    }

    @Description("初始化方法节点")
    private void initMethodNodes(PsiClass controller, Module module, PackageNode parentNode) {
        String controllerPath = PsiTool.getControllerPath(controller);
        String contextPath = moduleNodeMap.get(module.getName()).getValue().getContextPath();
        ClassNode classNode = new ClassNode(new ClassNodeData(controller));
        // 添加方法结点
        List<MethodNodeData> mappingMethods = PsiTool.getMappingMethods(controller, contextPath, controllerPath);
        mappingMethods.forEach(o -> classNode.add(new MethodNode(o)));
        parentNode.add(classNode);
    }

    @Description("获取字符串公共前缀")
    private String getCommonPrefix(List<String> packageNames) {
        if (packageNames.isEmpty()) {
            return "";
        }
        if (packageNames.size() == 1) {
            return packageNames.get(0);
        }
        String commonPrefix = packageNames.get(0);
        for (int i = 1; i < packageNames.size(); i++) {
            String currentString = packageNames.get(i);
            int j = 0;
            while (j < commonPrefix.length() && j < currentString.length() && commonPrefix.charAt(j) == currentString.charAt(j)) {
                j++;
            }
            commonPrefix = commonPrefix.substring(0, j);
            if (commonPrefix.isEmpty()) {
                break;
            }
        }
        return commonPrefix;
    }


    public void render(ModuleNode root) {
        super.getTreeModel().setRoot(root);
    }
}
