package com.zys.http.ui.tree;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.treeStructure.SimpleTree;
import com.zys.http.entity.tree.*;
import com.zys.http.tool.PsiTool;
import com.zys.http.ui.tree.node.*;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zys
 * @since 2023-09-08
 */
public class HttpApiTreePanel extends AbstractListTreePanel {

    private final transient Project project;

    private final transient Map<PsiMethod, MethodNode> methodNodes = new HashMap<>();
    private final transient Map<String, ModuleNode> moduleNodeMap = new HashMap<>();
    private final transient Map<String, PackageNode> packageNodeMap = new HashMap<>();

    public HttpApiTreePanel(@NotNull Project project) {
        super(new SimpleTree());
        this.project = project;
        initNode();
    }

    private void initNode() {
        Module[] modules = ModuleManager.getInstance(project).getSortedModules();
        String contextPath = PsiTool.getContextPath(project, modules[0]);
        ModuleNode root = new ModuleNode(new ModuleNodeData(modules[0].getName(), contextPath));
        getTreeModel().setRoot(root);
        if (modules.length == 1) {
            moduleNodeMap.put(modules[0].getName(), root);
            initModulesNodes(root, modules[0]);
            return;
        }

        for (Module module : modules) {
            String moduleName = module.getName();
            String parentName = ModuleRootManager.getInstance(module).getContentRoots()[0].getParent().getName();
            // 顶级模块跳过, 模块没有 Controller 类跳过
            List<PsiClass> controllers = PsiTool.getModuleController(project, module);
            //
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
            initModulesNodes(moduleNode, module);
        }
    }

    private void initModulesNodes(BaseNode<? extends NodeData> parent, Module module){
        List<PsiClass> controllers = PsiTool.getModuleController(project, module);
        List<String> packageNames = controllers.stream().map(PsiClass::getQualifiedName)
                .filter(Objects::nonNull).map(o -> o.substring(0, o.lastIndexOf('.'))).toList();

        String commonPrefix = getCommonPrefix(packageNames);
        String finalCommonPrefix = commonPrefix;
        if (CharSequenceUtil.isNotEmpty(commonPrefix)) {
            if (commonPrefix.endsWith(".")) {
                commonPrefix = commonPrefix.substring(0, commonPrefix.length() - 1);
            }
            PackageNode commonPackageNode = new PackageNode(new PackageNodeData(commonPrefix));
            // 是否有类在当前节点, 添加
            for (PsiClass controller : controllers) {
                String qualifiedName = controller.getQualifiedName();
                if (qualifiedName != null) {
                    qualifiedName = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
                    if (qualifiedName.equals(finalCommonPrefix)) {
                        String controllerPath = PsiTool.getControllerPath(controller);
                        String contextPath = moduleNodeMap.get(module.getName()).getValue().getContextPath();
                        ClassNode classNode = new ClassNode(new ClassNodeData(controller, contextPath, controllerPath));
                        commonPackageNode.add(classNode);
                        // 添加方法结点
                    }
                }
            }
            parent.add(commonPackageNode);
            // 剩余的进行分组
            Map<String, Set<String>> map = packageNames.stream().filter(o -> o.length() > finalCommonPrefix.length())
                    .map(o -> o.substring(finalCommonPrefix.length()))
                    .collect(Collectors.groupingBy(
                            o -> o.substring(0, o.lastIndexOf('.')), Collectors.mapping(
                                    o -> o.substring(o.lastIndexOf('.') + 1), Collectors.toSet()
                            )
                    ));
            map.forEach((k, v) -> {
                if (v.size() > 1) {
                    PackageNode node = new PackageNode(new PackageNodeData(k));
                    for (String s : v) {
                        node.add(new PackageNode(new PackageNodeData(s)));
                    }
                    commonPackageNode.add(node);
                } else {
                    commonPackageNode.add(new PackageNode(new PackageNodeData(k + "." + v)));
                }
            });
        }
    }

    private void initNodes() {
        String projectName = project.getName();
        String contextPath = Arrays.stream(ModuleManager.getInstance(project).getModules())
                .filter(o -> projectName.equals(o.getName()))
                .map(o -> PsiTool.getContextPath(project, o)).findFirst().orElse("");
        ModuleNode root = new ModuleNode(new ModuleNodeData(projectName, contextPath));
        super.getTreeModel().setRoot(root);
        initModuleNodes();
        // initPackageNodes();
        // initClassNodes();
        initMethodNodes();
    }

    @Description("初始化项目模块节点")
    private void initModuleNodes() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) super.getTreeModel().getRoot();
        initModuleNodes(project, project.getName(), PsiTool.buildModuleLayer(project)).forEach(root::add);
    }

    @Description("初始化项目模块节点")
    private List<BaseNode<? extends NodeData>> initModuleNodes(@NotNull Project project, String name, @NotNull Map<String, List<String>> modules) {
        List<String> subModuleNames = modules.get(name);
        if (Objects.isNull(subModuleNames)) {
            return Collections.emptyList();
        }
        List<BaseNode<? extends NodeData>> moduleNodes = new ArrayList<>();
        for (String childName : subModuleNames) {
            String contextPath = PsiTool.getContextPath(project, Objects.requireNonNull(PsiTool.getModuleByName(project, childName)));
            ModuleNodeData nodeData = new ModuleNodeData(childName, contextPath);
            ModuleNode moduleNode = new ModuleNode(nodeData);
            List<BaseNode<? extends NodeData>> childrenNodes = initModuleNodes(project, childName, modules);
            childrenNodes.forEach(moduleNode::add);
            moduleNodes.add(moduleNode);
        }
        return moduleNodes;
    }

    // @Description("初始化包结点")
    // private void initPackageNodes() {
    //     List<DefaultMutableTreeNode> allLeafNodes = getAllLeafNodes();
    //     for (DefaultMutableTreeNode node : allLeafNodes) {
    //         String nodeName = ((BaseNode<? extends NodeData>) node).getValue().getNodeName();
    //         List<PsiClass> controller = PsiTool.getModuleController(project, PsiTool.getModuleByName(project, nodeName));
    //         if (controller.isEmpty()) {
    //             // 说明模块没有使用 @Controller 和 @RestController
    //             TreeNode parent = node.getParent();
    //             if (!node.isRoot()) {
    //                 getTreeModel().removeNodeFromParent(node);
    //             }
    //             if (parent != null && parent.isLeaf()) {
    //                 getTreeModel().removeNodeFromParent((MutableTreeNode) parent);
    //             }
    //             continue;
    //         }
    //         List<String> classNames = controller.stream().map(PsiClass::getQualifiedName).toList();
    //         String classCommonPackagePrefix = getClassCommonPackagePrefix(classNames);
    //         PackageNode commonPackageNode;
    //         if (CharSequenceUtil.isNotEmpty(classCommonPackagePrefix)) {
    //             if (classCommonPackagePrefix.endsWith(".")) {
    //                 classCommonPackagePrefix = classCommonPackagePrefix.substring(0, classCommonPackagePrefix.length() - 1);
    //             }
    //
    //             commonPackageNode = new PackageNode(new PackageNodeData(classCommonPackagePrefix, nodeName));
    //             node.add(commonPackageNode);
    //
    //             String finalClassCommonPackagePrefix = classCommonPackagePrefix;
    //             Map<String, List<String>> map = classNames.stream()
    //                     .map(o -> o.substring(0, o.lastIndexOf(".")))
    //                     .map(o -> o.substring(finalClassCommonPackagePrefix.length()))
    //                     .filter(o -> !o.isEmpty())
    //                     .collect(Collectors.groupingBy(o -> o));
    //             for (List<String> value : map.values()) {
    //                 if (value.isEmpty()) {
    //                     continue;
    //                 }
    //                 if (value.size() == 1) {
    //                     String a = value.get(0);
    //                     a = a.startsWith(".") ? a.substring(1) : a;
    //                     if (!a.trim().isEmpty()) {
    //                         commonPackageNode.add(new PackageNode(new PackageNodeData(a, nodeName)));
    //                     }
    //                 }
    //                 String commonPrefix = getCommonPrefix(value).trim();
    //                 if (!commonPrefix.isEmpty()) {
    //                     commonPrefix = commonPrefix.startsWith(".") ? commonPrefix.substring(1) : commonPrefix;
    //                     if (Objects.isNull(findNodeByContent((BaseNode<? extends NodeData>) node, commonPrefix))) {
    //                         PackageNodeData nodeData = new PackageNodeData(commonPrefix, nodeName);
    //                         PackageNode packageNode = new PackageNode(nodeData);
    //                         if (value.get(0).length() > commonPrefix.length()) {
    //                             String rest = value.get(0);
    //                             rest = rest.startsWith(".") ? rest.substring(1) : rest;
    //                             rest = rest.substring(commonPrefix.length());
    //                             if (!rest.isEmpty()) {
    //                                 PackageNodeData restNodeData = new PackageNodeData(rest, nodeName);
    //                                 packageNode.add(new PackageNode(restNodeData));
    //                             }
    //
    //                         }
    //                         commonPackageNode.add(packageNode);
    //                     }
    //                 }
    //             }
    //         }
    //     }
    // }
    //
    // @Description("初始化类结点")
    // private void initClassNodes() {
    //     List<DefaultMutableTreeNode> allLeafNodes = getAllLeafNodes();
    //     for (DefaultMutableTreeNode node : allLeafNodes) {
    //         String moduleName = ((PackageNode) node).getValue().getModuleName();
    //         String nodeName = ((PackageNode) node).getValue().getNodeName();
    //         List<PsiClass> controller = PsiTool.getModuleController(project, PsiTool.getModuleByName(project, moduleName));
    //         if (controller.isEmpty()) {
    //             continue;
    //         }
    //         for (PsiClass psiClass : controller) {
    //             String qualifiedName = psiClass.getQualifiedName();
    //             if (CharSequenceUtil.isEmpty(qualifiedName)) {
    //                 continue;
    //             }
    //             qualifiedName = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
    //             String contextPath = PsiTool.getContextPath(project, Objects.requireNonNull(PsiTool.getModuleByName(project, moduleName)));
    //             ClassNodeData classNodeData = new ClassNodeData(psiClass, contextPath, PsiTool.getControllerPath(psiClass));
    //             if (qualifiedName.endsWith(nodeName)) {
    //                 node.add(new ClassNode(classNodeData));
    //             } else {
    //                 DefaultMutableTreeNode treeNode = findNodeByContent((BaseNode<? extends NodeData>) super.getTreeModel().getRoot(), qualifiedName);
    //                 if (Objects.nonNull(treeNode)) {
    //                     treeNode.add(new ClassNode(classNodeData));
    //                 }
    //             }
    //         }
    //     }
    // }

    @Description("初始化方法结点")
    private void initMethodNodes() {
        List<DefaultMutableTreeNode> classNodes = getAllLeafNodes();
        if (classNodes.size() == 1 && !(classNodes.get(0) instanceof ClassNode)) {
            // 说明当前项目没有引用 SpringBoot 项目
            return;
        }
        for (DefaultMutableTreeNode classNode : classNodes) {
            ClassNode node = (ClassNode) classNode;
            ClassNodeData value = node.getValue();
            PsiClass psiClass = value.getPsiClass();
            List<MethodNodeData> mappingMethods = PsiTool.getMappingMethods(psiClass, value.getContextPath(), PsiTool.getControllerPath(psiClass));
            mappingMethods.forEach(o -> classNode.add(new MethodNode(o)));
        }
    }

    @Description("获取类的公共包名")
    private String getClassCommonPackagePrefix(List<String> psiClassList) {
        if (psiClassList.isEmpty()) {
            return "";
        }
        List<String> packageNames = psiClassList.stream().filter(Objects::nonNull)
                .map(v -> v.substring(0, v.lastIndexOf('.'))).toList();
        return getCommonPrefix(packageNames);
    }

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

    @Description("根据节点展示内容找到指定结点")
    private DefaultMutableTreeNode findNodeByContent(BaseNode<? extends NodeData> node, String targetContent) {
        if (node.getFragment().equals(targetContent)) {
            return node;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            BaseNode<? extends NodeData> childNode = (BaseNode<? extends NodeData>) node.getChildAt(i);
            DefaultMutableTreeNode foundNode = findNodeByContent(childNode, targetContent);
            if (foundNode != null) {
                return foundNode;
            }
        }
        return null;
    }

    @Description("获取所有的叶子节点")
    private List<DefaultMutableTreeNode> getAllLeafNodes() {
        List<DefaultMutableTreeNode> leafNodes = new ArrayList<>();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) super.getTreeModel().getRoot();
        getAllLeafNodes(root, leafNodes);
        return leafNodes;
    }

    private void getAllLeafNodes(DefaultMutableTreeNode node, List<DefaultMutableTreeNode> leafNodes) {
        if (node.isLeaf()) {
            leafNodes.add(node);
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                getAllLeafNodes(childNode, leafNodes);
            }
        }
    }
}
