package com.zys.http.ui.tree;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiClass;
import com.intellij.ui.treeStructure.SimpleTree;
import com.zys.http.entity.tree.ClassNodeData;
import com.zys.http.entity.tree.ModuleNodeData;
import com.zys.http.entity.tree.NodeData;
import com.zys.http.entity.tree.PackageNodeData;
import com.zys.http.tool.PsiTool;
import com.zys.http.ui.tree.node.*;
import jdk.jfr.Description;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreeNode;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author zys
 * @since 2023-09-08
 */
public class HttpApiTreePanel extends AbstractListTreePanel {

    private final transient Project project;

    @Description("模块名, 模块结点")
    private final transient Map<String, ModuleNode> moduleNodeMap = new HashMap<>();

    @Description("模块名, controller")
    private final transient Map<String, List<PsiClass>> classNodeMap = new HashMap<>();

    @Description("controller, 方法列表")
    private final transient Map<PsiClass, List<MethodNode>> methodNodeMap = new HashMap<>();

    @Getter
    @Setter
    @Description("选中方法节点后的回调")
    private transient Consumer<MethodNode> chooseCallback;

    public HttpApiTreePanel(@NotNull Project project) {
        super(new SimpleTree());
        this.project = project;
    }

    public ModuleNode initNodes() {
        return initModuleNodes();
    }

    @Description("初始化模块结点, 可能有多层级")
    private ModuleNode initModuleNodes() {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        String projectName = project.getName();
        Module rootModule = Stream.of(modules).filter(o -> o.getName().equals(projectName)).findFirst().orElse(null);
        if (Objects.isNull(rootModule)) {
            return new ModuleNode(new ModuleNodeData("", ""));
        }
        String contextPath = PsiTool.getContextPath(project, rootModule);
        ModuleNode rootNode = new ModuleNode(new ModuleNodeData(projectName, contextPath));
        moduleNodeMap.put(rootModule.getName(), rootNode);
        List<PsiClass> controllers = PsiTool.getModuleController(project, rootModule);
        if (!controllers.isEmpty()) {
            classNodeMap.put(projectName, controllers);
            for (PsiClass c : controllers) {
                String controllerPath = PsiTool.getControllerPath(c);
                methodNodeMap.put(c, PsiTool.getMappingMethods(c, contextPath, controllerPath));
            }
        }
        super.getTreeModel().setRoot(rootNode);
        List<Module> list = Stream.of(modules).filter(o -> !o.getName().equals(projectName)).distinct()
                .peek(o -> {
                    String contextPath1 = PsiTool.getContextPath(project, o);
                    ModuleNode moduleNode = new ModuleNode(new ModuleNodeData(o.getName(), contextPath1));
                    moduleNodeMap.put(o.getName(), moduleNode);
                })
                .toList();

        for (Module o : list) {
            String moduleName = o.getName();
            String parentName = ModuleRootManager.getInstance(o).getContentRoots()[0].getParent().getName();
            String contextPath1 = PsiTool.getContextPath(project, o);
            ModuleNode parentNode = moduleNodeMap.get(parentName);
            ModuleNode moduleNode = moduleNodeMap.get(moduleName);
            if (!"Project".equals(parentName) && Objects.nonNull(parentNode)) {
                parentNode.add(moduleNode);
            }
            controllers = PsiTool.getModuleController(project, o);
            classNodeMap.put(moduleName, controllers);
            for (PsiClass c : controllers) {
                String controllerPath = PsiTool.getControllerPath(c);
                methodNodeMap.put(c, PsiTool.getMappingMethods(c, contextPath1, controllerPath));
            }
            initPackageNodes(moduleName).forEach(moduleNode::add);
        }
        if (moduleNodeMap.size() == 1) {

            initPackageNodes(projectName).forEach(rootNode::add);
        }
        return rootNode;
    }

    @Description("初始化包结点、类结点、方法结点")
    private List<BaseNode<? extends NodeData>> initPackageNodes(String moduleName) {
        List<PsiClass> psiClasses = classNodeMap.get(moduleName);

        if (methodNodeMap.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, PackageNode> packageNodeMap = new HashMap<>(4);
        List<BaseNode<?>> children = new ArrayList<>();
        List<BaseNode<?>> unKnownPackage = new ArrayList<>(0);

        for (Map.Entry<PsiClass, List<MethodNode>> e : methodNodeMap.entrySet()) {
            PsiClass k = e.getKey();
            List<MethodNode> v = e.getValue();

            if (!psiClasses.contains(k)) {
                continue;
            }
            ClassNode classNode = new ClassNode(new ClassNodeData(k));
            v.forEach(classNode::add);
            String packageName = PsiTool.getPackageName(k);
            if (packageName == null) {
                // 没有包名则直接添加到 module 节点
                unKnownPackage.add(classNode);
            } else {
                customPending(packageNodeMap, packageName).add(classNode);
            }
        }

        List<PackageNode> nodes = new ArrayList<>();
        packageNodeMap.forEach((key, rootNode) -> {
            while (true) {
                List<PackageNode> list = findChildren(rootNode);
                if (list.size() == 1) {
                    PackageNode newEle = list.get(0);
                    rootNode.remove(newEle);
                    newEle.getValue().setNodeName(rootNode.getValue().getNodeName() + "." + newEle.getValue().getNodeName());
                    rootNode = newEle;
                } else {
                    break;
                }
            }

            nodes.add(rootNode);
        });
        if (!unKnownPackage.isEmpty()) {
            unKnownPackage.sort(Comparator.comparing(BaseNode::toString));
            children.addAll(unKnownPackage);
        }

        children.addAll(nodes);
        return children;
    }

    public void render(ModuleNode root) {
        super.getTreeModel().setRoot(root);
    }

    private static PackageNode customPending(@NotNull Map<String, PackageNode> data, @NotNull String packageName) {
        String[] names = packageName.split("\\.");

        PackageNode node = data.computeIfAbsent(names[0], o -> new PackageNode(new PackageNodeData(o)));

        if (names.length == 1) {
            return node;
        }

        PackageNode curr = node;
        int fex = 1;
        while (fex < names.length) {
            String name = names[fex++];
            curr = findChild(curr, name);
        }

        return curr;
    }

    private static @NotNull PackageNode findChild(@NotNull PackageNode node, @NotNull String name) {
        Enumeration<TreeNode> children = node.children();
        while (children.hasMoreElements()) {
            TreeNode child = children.nextElement();
            if (!(child instanceof PackageNode packageNode)) {
                continue;
            }
            if (name.equals(packageNode.getValue().getNodeName())) {
                return packageNode;
            }
        }
        PackageNode packageNode = new PackageNode(new PackageNodeData(name));
        node.add(packageNode);
        return packageNode;
    }

    private static @NotNull List<PackageNode> findChildren(@NotNull PackageNode node) {
        List<PackageNode> children = new ArrayList<>();
        Enumeration<TreeNode> enumeration = node.children();
        while (enumeration.hasMoreElements()) {
            TreeNode ele = enumeration.nextElement();
            if (ele instanceof PackageNode p) {
                children.add(p);
            }
        }

        return children;
    }


    @Override
    protected @Nullable Consumer<BaseNode<?>> getChooseListener() {
        return node -> {
            if (node instanceof MethodNode methodNode && Objects.nonNull(chooseCallback)) {
                chooseCallback.accept(methodNode);
            }
        };
    }

    @Override
    protected @Nullable Consumer<BaseNode<?>> getDoubleClickListener() {
        return null;
    }
}
