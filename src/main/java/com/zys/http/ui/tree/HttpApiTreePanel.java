package com.zys.http.ui.tree;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.treeStructure.SimpleTree;
import com.zys.http.constant.HttpEnum;
import com.zys.http.entity.HttpConfig;
import com.zys.http.entity.tree.ClassNodeData;
import com.zys.http.entity.tree.ModuleNodeData;
import com.zys.http.entity.tree.NodeData;
import com.zys.http.entity.tree.PackageNodeData;
import com.zys.http.tool.HttpPropertyTool;
import com.zys.http.tool.ProjectTool;
import com.zys.http.tool.PsiTool;
import com.zys.http.tool.TreeTool;
import com.zys.http.ui.tree.node.*;
import jdk.jfr.Description;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author zys
 * @since 2023-09-08
 */
@Description("树形列表展示区")
public class HttpApiTreePanel extends AbstractListTreePanel {

    private final transient Project project;
    private final transient HttpPropertyTool httpPropertyTool;

    @Description("模块名, 模块结点")
    private final transient Map<String, ModuleNode> moduleNodeMap = new HashMap<>();

    @Description("模块名, controller")
    private final transient Map<String, List<PsiClass>> moduleControllerMap = new HashMap<>();

    @Description("controller, 方法列表")
    private final transient Map<PsiClass, List<MethodNode>> methodNodeMap = new HashMap<>();
    @Description("方法引用, 方法结点")
    private final transient Map<PsiMethod, MethodNode> methodNodePsiMap = new HashMap<>();


    @Getter
    @Setter
    @Description("选中方法节点后的回调")
    private transient Consumer<MethodNode> chooseCallback;

    public HttpApiTreePanel(@NotNull Project project) {
        super(new SimpleTree());
        this.project = project;
        this.httpPropertyTool = HttpPropertyTool.getInstance(project);
    }

    public ModuleNode initNodes() {
        return initModuleNodes();
    }

    @Description("初始化模块结点, 可能有多层级")
    private ModuleNode initModuleNodes() {
        Collection<Module> modules = ProjectTool.moduleList(project);
        Module rootModule = ProjectTool.getRootModule(project);
        if (modules.isEmpty() || Objects.isNull(rootModule)) {
            return new ModuleNode(new ModuleNodeData("Empty Module", ""));
        }

        String contextPath;
        // 初始化所有的模块结点
        for (Module m : modules) {
            contextPath = ProjectTool.getModuleContextPath(project, m);
            moduleNodeMap.put(m.getName(), new ModuleNode(new ModuleNodeData(m.getName(), contextPath)));
        }

        String moduleName;
        String parentName;
        VirtualFile[] contentRoots;
        ModuleNode pn;
        ModuleNode mn;
        List<PsiClass> controllers;
        // 构建模块结点层级
        for (Module m : modules) {
            moduleName = m.getName();
            contentRoots = ModuleRootManager.getInstance(m).getContentRoots();
            if (contentRoots.length < 1) {
                continue;
            }
            // 获取当前模块的父模块名称
            parentName = contentRoots[0].getParent().getName();
            pn = moduleNodeMap.get(parentName);
            mn = moduleNodeMap.get(moduleName);
            if (!"Project".equals(parentName) && Objects.nonNull(pn)) {
                pn.add(mn);
            }

            controllers = ProjectTool.getModuleControllers(project, m);
            if (!controllers.isEmpty()) {
                moduleControllerMap.put(moduleName, controllers);
                String host = "127.0.0.1:" + ProjectTool.getModulePort(project, m);
                httpPropertyTool.putHttpConfig(moduleName, new HttpConfig(HttpEnum.Protocol.HTTP, host, Collections.emptyMap()));
                String controllerPath;
                for (PsiClass c : controllers) {
                    controllerPath = PsiTool.getControllerPath(c);
                    contextPath = ProjectTool.getModuleContextPath(project, m);
                    methodNodeMap.put(c, PsiTool.getMappingMethods(c, contextPath, controllerPath, methodNodePsiMap));
                }
                initPackageNodes(moduleName).forEach(mn::add);
            }
        }

        removeEmptyModule();
        return moduleNodeMap.get(project.getName());
    }

    @Description("初始化包结点、类结点、方法结点")
    private List<BaseNode<? extends NodeData>> initPackageNodes(String moduleName) {
        // 获取当前模块的所有 controller
        List<PsiClass> psiClasses = moduleControllerMap.get(moduleName);

        if (Objects.isNull(psiClasses) || methodNodeMap.isEmpty()) {
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
            ClassNodeData data = new ClassNodeData(k);
            String s = PsiTool.getSwaggerAnnotation(k, "CLASS_");
            if (CharSequenceUtil.isNotEmpty(s)) {
                data.setDescription(s);
            }
            ClassNode classNode = new ClassNode(data);

            v.forEach(classNode::add);
            String packageName = PsiTool.getPackageName(k);
            if (Objects.isNull(packageName)) {
                // 没有包名则直接添加到 module 节点
                unKnownPackage.add(classNode);
            } else {
                createPackageNodes(packageNodeMap, packageName).add(classNode);
            }
        }

        List<PackageNode> nodes = new ArrayList<>();
        packageNodeMap.forEach((key, rootNode) -> {
            while (true) {
                List<BaseNode<?>> list = TreeTool.findChildren(rootNode);
                if (list.size() == 1) {
                    PackageNode newEle = (PackageNode) list.get(0);
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


    private PackageNode createPackageNodes(@NotNull Map<String, PackageNode> data, @NotNull String packageName) {
        String[] names = packageName.split("\\.");
        PackageNode node = data.computeIfAbsent(names[0], o -> new PackageNode(new PackageNodeData(o)));
        if (names.length == 1) {
            return node;
        }
        PackageNode curr = node;
        int fex = 1;
        while (fex < names.length) {
            String name = names[fex++];
            PackageNode child = (PackageNode) TreeTool.findChild(curr, name);
            if (Objects.isNull(child)) {
                child = new PackageNode(new PackageNodeData(name));
                curr.add(child);
            }
            curr = child;
        }
        return curr;
    }



    @Description("移除空模块节点")
    private void removeEmptyModule() {
        for (Map.Entry<String, ModuleNode> entry : moduleNodeMap.entrySet()) {
            ModuleNode value = entry.getValue();
            if (!value.isRoot() && value.isLeaf()) {
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) value.getParent();
                getTreeModel().removeNodeFromParent(value);
                if (parent != null && parent.isLeaf() && !parent.isRoot()) {
                    getTreeModel().removeNodeFromParent(parent);
                }
            }
        }
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
    @Description("双击跳转到指定的方法")
    protected @Nullable Consumer<BaseNode<?>> getDoubleClickListener() {
        return node -> {
            if (node instanceof MethodNode m) {
                NavigatablePsiElement psiElement = m.getValue().getPsiElement();
                if (Objects.nonNull(psiElement)) {
                    psiElement.navigate(true);
                }
            }
        };
    }


}
