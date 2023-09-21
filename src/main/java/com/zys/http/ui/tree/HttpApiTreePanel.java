package com.zys.http.ui.tree;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.treeStructure.SimpleTree;
import com.zys.http.action.CommonAction;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.SpringEnum;
import com.zys.http.entity.HttpConfig;
import com.zys.http.entity.tree.*;
import com.zys.http.service.Bundle;
import com.zys.http.service.NotifyService;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.tool.ProjectTool;
import com.zys.http.tool.PsiTool;
import com.zys.http.tool.SystemTool;
import com.zys.http.tool.ui.TreeTool;
import com.zys.http.ui.icon.HttpIcons;
import com.zys.http.ui.tree.node.*;
import jdk.jfr.Description;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author zys
 * @since 2023-09-08
 */
@Description("树形列表展示区")
public class HttpApiTreePanel extends AbstractListTreePanel {


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

    public HttpApiTreePanel() {
        super(new SimpleTree());
    }

    public ModuleNode initNodes(List<HttpEnum.HttpMethod> methods) {
        return initModuleNodes(methods);
    }

    @Description("初始化模块结点, 可能有多层级")
    private ModuleNode initModuleNodes(List<HttpEnum.HttpMethod> methods) {
        Project project = ProjectTool.getProject();
        Collection<Module> modules = ProjectTool.moduleList();
        Module rootModule = ProjectTool.getRootModule();
        if (modules.isEmpty() || Objects.isNull(rootModule)) {
            return new ModuleNode(new ModuleNodeData("Empty Module", ""));
        }

        String contextPath;
        // 初始化所有的模块结点
        for (Module m : modules) {
            contextPath = ProjectTool.getModuleContextPath(m);
            moduleNodeMap.put(m.getName(), new ModuleNode(new ModuleNodeData(m.getName(), contextPath)));
        }

        if (methods.isEmpty()) {
            return moduleNodeMap.get(project.getName());
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

            controllers = ProjectTool.getModuleControllers( m);
            if (!controllers.isEmpty()) {
                moduleControllerMap.put(moduleName, controllers);
                String host = "127.0.0.1:" + ProjectTool.getModulePort( m);
                HttpServiceTool.putHttpConfig(moduleName, new HttpConfig(HttpEnum.Protocol.HTTP, host, Collections.emptyMap()));
                String controllerPath;
                for (PsiClass c : controllers) {
                    controllerPath = PsiTool.getControllerPath(c);
                    contextPath = ProjectTool.getModuleContextPath( m);
                    methodNodeMap.put(c, buildMethodNodes(c, contextPath, controllerPath, methodNodePsiMap));
                }
                initPackageNodes(moduleName, methods).forEach(mn::add);
            }
        }

        removeEmptyModule();
        return moduleNodeMap.get(project.getName());
    }

    @Description("初始化包结点、类结点、方法结点")
    private List<BaseNode<? extends NodeData>> initPackageNodes(String moduleName, List<HttpEnum.HttpMethod> methods) {
        // 获取当前模块的所有 controller
        List<PsiClass> psiClasses = moduleControllerMap.get(moduleName);

        if (Objects.isNull(psiClasses) || psiClasses.isEmpty()) {
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
            data.setDescription(s);
            ClassNode classNode = new ClassNode(data);

            v.forEach(methodNode -> {
                HttpEnum.HttpMethod httpMethod = methodNode.getValue().getHttpMethod();
                if (methods.contains(httpMethod)) {
                    classNode.add(methodNode);
                }
            });
            String packageName = PsiTool.getPackageName(k);
            if (classNode.getChildCount() > 0) {
                if (Objects.isNull(packageName)) {
                    // 没有包名则直接添加到 module 节点
                    unKnownPackage.add(classNode);
                } else {
                    createPackageNodes(packageNodeMap, packageName).add(classNode);
                }
            }
        }

        List<PackageNode> nodes = new ArrayList<>();
        packageNodeMap.forEach((key, rootNode) -> {
            while (true) {
                // 判断当前结点的包结点是否只有一个
                List<BaseNode<?>> list = TreeTool.findChildren(rootNode);
                List<BaseNode<?>> packageList = list.stream().filter(PackageNode.class::isInstance).toList();
                if (list.size() == 1 && packageList.size() == 1) {
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
                if (Objects.nonNull(parent) && parent.isLeaf() && !parent.isRoot()) {
                    getTreeModel().removeNodeFromParent(parent);
                }
            }
        }
    }


    @Override
    @Nullable
    protected Consumer<BaseNode<?>> getChooseListener() {
        return node -> {
            if (node instanceof MethodNode methodNode && Objects.nonNull(chooseCallback)) {
                chooseCallback.accept(methodNode);
            }
        };
    }

    @Override
    @Nullable
    @Description("双击跳转到指定的方法")
    protected Consumer<BaseNode<?>> getDoubleClickListener() {
        return node -> {
            if (node instanceof MethodNode m) {
                NavigatablePsiElement psiElement = m.getValue().getPsiElement();
                if (Objects.nonNull(psiElement)) {
                    psiElement.navigate(true);
                }
            }
        };
    }

    @Override
    protected @Nullable JPopupMenu getRightClickMenu(@NotNull MouseEvent e, @NotNull BaseNode<?> node) {
        if (!(node instanceof MethodNode mn)) {
            return null;
        }
        DefaultActionGroup group = new DefaultActionGroup();
        group.setPopup(true);

        CommonAction navigation = new CommonAction(Bundle.get("http.tree.right.item.navigation"), "", HttpIcons.General.LOCATE);
        navigation.setAction(event -> mn.getValue().getPsiElement().navigate(true));
        group.add(navigation);

        CommonAction copyFullPath = new CommonAction(Bundle.get("http.tree.right.item.copy.full.path"), "", AllIcons.Actions.Copy);
        copyFullPath.setAction(event -> {
            HttpConfig config = HttpServiceTool.getDefaultHttpConfig();
            String protocol = config.getProtocol().name().toLowerCase();
            SystemTool.copy2Clipboard(protocol + "://" + config.getHostValue() + mn.getFragment());
            NotifyService.instance(ProjectTool.getProject()).info(Bundle.get("http.tree.right.item.copy.full.msg"));
        });
        group.add(copyFullPath);

        CommonAction copyApiPath = new CommonAction(Bundle.get("http.tree.right.item.copy.api.path"), "", AllIcons.Actions.Copy);
        copyApiPath.setAction(event -> {
            SystemTool.copy2Clipboard(mn.getFragment());
            NotifyService.instance(ProjectTool.getProject()).info(Bundle.get("http.tree.right.item.copy.api.msg"));
        });
        group.add(copyApiPath);

        return ActionManager.getInstance().createActionPopupMenu("http", group).getComponent();
    }

    @Description("获取所有 @xxxMapping 的方法")
    public List<MethodNode> buildMethodNodes(@NotNull PsiClass psiClass, String contextPath, String controllerPath, Map<PsiMethod, MethodNode> methodNodePsiMap) {
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
                    data1.setDescription(PsiTool.getSwaggerAnnotation(method, "METHOD_"));
                    dataList.add(data);
                    methodNodePsiMap.put(method, data);
                }
            }
        }
        return dataList;
    }

    private MethodNodeData buildMethodNodeData(@NotNull PsiAnnotation annotation, String contextPath, String controllerPath, PsiMethod psiElement) {
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
        String name = PsiTool.getAnnotationValue(annotation, new String[]{"value", "path"});
        MethodNodeData data = new MethodNodeData(httpMethod, name, controllerPath, contextPath);
        data.setPsiElement(psiElement);
        return data;
    }
}
