package com.zys.http.ui.tree;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.treeStructure.SimpleTree;
import com.zys.http.entity.tree.*;
import com.zys.http.tool.PsiTool;
import com.zys.http.ui.tree.node.*;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zys
 * @since 2023-09-08
 */
public class HttpApiTreePanel extends AbstractListTreePanel {

    private final transient Project project;

    private final transient Map<PsiMethod, MethodNode> methodNodes = new HashMap<>();

    public HttpApiTreePanel(@NotNull Project project) {
        super(new SimpleTree());
        this.project = project;
        initNodes();
    }

    private void initNodes() {
        String projectName = project.getName();
        String contextPath = Arrays.stream(ModuleManager.getInstance(project).getModules())
                .filter(o -> projectName.equals(o.getName()))
                .map(o -> PsiTool.getContextPath(project, o)).findFirst().orElse("");
        ModuleNode root = new ModuleNode(new ModuleNodeData(projectName, contextPath));
        super.getTreeModel().setRoot(root);
        initModuleNodes();
        initPackageNodes();
        initClassNodes();
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

    @Description("初始化包结点")
    private void initPackageNodes() {
        List<DefaultMutableTreeNode> allLeafNodes = getAllLeafNodes();
        for (DefaultMutableTreeNode node : allLeafNodes) {
            String nodeName = ((BaseNode<? extends NodeData>) node).getValue().getNodeName();
            List<PsiClass> controller = PsiTool.getModuleController(project, PsiTool.getModuleByName(project, nodeName));
            if (controller.isEmpty()) {
                // 说明模块没有使用 @Controller 和 @RestController
                TreeNode parent = node.getParent();
                if (!node.isRoot()) {
                    getTreeModel().removeNodeFromParent(node);
                }
                if (parent != null && parent.isLeaf()) {
                    getTreeModel().removeNodeFromParent((MutableTreeNode) parent);
                }
                continue;
            }
            List<String> classNames = controller.stream().map(PsiClass::getQualifiedName).toList();
            String classCommonPackagePrefix = getClassCommonPackagePrefix(classNames);
            PackageNode commonPackageNode;
            if (CharSequenceUtil.isNotEmpty(classCommonPackagePrefix)) {
                if (classCommonPackagePrefix.endsWith(".")) {
                    classCommonPackagePrefix = classCommonPackagePrefix.substring(0, classCommonPackagePrefix.length() - 1);
                }

                commonPackageNode = new PackageNode(new PackageNodeData(classCommonPackagePrefix, nodeName));
                node.add(commonPackageNode);

                String finalClassCommonPackagePrefix = classCommonPackagePrefix;
                Map<String, List<String>> map = classNames.stream()
                        .map(o -> o.substring(0, o.lastIndexOf(".")))
                        .map(o -> o.substring(finalClassCommonPackagePrefix.length()))
                        .filter(o -> !o.isEmpty())
                        .collect(Collectors.groupingBy(o -> o));
                for (List<String> value : map.values()) {
                    if (value.isEmpty()) {
                        continue;
                    }
                    if (value.size() == 1) {
                        String a = value.get(0);
                        a = a.startsWith(".") ? a.substring(1) : a;
                        if (!a.trim().isEmpty()) {
                            commonPackageNode.add(new PackageNode(new PackageNodeData(a, nodeName)));
                        }
                    }
                    String commonPrefix = getCommonPrefix(value).trim();
                    if (!commonPrefix.isEmpty()) {
                        commonPrefix = commonPrefix.startsWith(".") ? commonPrefix.substring(1) : commonPrefix;
                        if (Objects.isNull(findNodeByContent((BaseNode<? extends NodeData>) node, commonPrefix))) {
                            PackageNodeData nodeData = new PackageNodeData(commonPrefix, nodeName);
                            PackageNode packageNode = new PackageNode(nodeData);
                            if (value.get(0).length() > commonPrefix.length()) {
                                String rest = value.get(0);
                                rest = rest.startsWith(".") ? rest.substring(1) : rest;
                                rest = rest.substring(commonPrefix.length());
                                if (!rest.isEmpty()) {
                                    PackageNodeData restNodeData = new PackageNodeData(rest, nodeName);
                                    packageNode.add(new PackageNode(restNodeData));
                                }

                            }
                            commonPackageNode.add(packageNode);
                        }
                    }
                }
            }
        }
    }

    @Description("初始化类结点")
    private void initClassNodes() {
        List<DefaultMutableTreeNode> allLeafNodes = getAllLeafNodes();
        for (DefaultMutableTreeNode node : allLeafNodes) {
            String moduleName = ((PackageNode) node).getValue().getModuleName();
            String nodeName = ((PackageNode) node).getValue().getNodeName();
            List<PsiClass> controller = PsiTool.getModuleController(project, PsiTool.getModuleByName(project, moduleName));
            if (controller.isEmpty()) {
                continue;
            }
            for (PsiClass psiClass : controller) {
                String qualifiedName = psiClass.getQualifiedName();
                if (CharSequenceUtil.isEmpty(qualifiedName)) {
                    continue;
                }
                qualifiedName = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
                String contextPath = PsiTool.getContextPath(project, Objects.requireNonNull(PsiTool.getModuleByName(project, moduleName)));
                ClassNodeData classNodeData = new ClassNodeData(psiClass, contextPath, PsiTool.getControllerPath(psiClass));
                if (qualifiedName.endsWith(nodeName)) {
                    node.add(new ClassNode(classNodeData));
                } else {
                    DefaultMutableTreeNode treeNode = findNodeByContent((BaseNode<? extends NodeData>) super.getTreeModel().getRoot(), qualifiedName);
                    if (Objects.nonNull(treeNode)) {
                        treeNode.add(new ClassNode(classNodeData));
                    }
                }
            }
        }
    }

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
