package com.zys.http.ui.tree;

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
import com.zys.http.action.CopyAction;
import com.zys.http.action.ExpandAction;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.SpringEnum;
import com.zys.http.entity.HttpConfig;
import com.zys.http.entity.tree.*;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.service.NotifyService;
import com.zys.http.extension.setting.HttpSetting;
import com.zys.http.extension.topic.EnvListChangeTopic;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.tool.ProjectTool;
import com.zys.http.tool.PsiTool;
import com.zys.http.tool.SystemTool;
import com.zys.http.tool.ui.ThemeTool;
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

import static com.zys.http.ui.popup.NodeShowFilterPopup.SETTING_VALUES;

/**
 * @author zys
 * @since 2023-09-08
 */
@Description("树形列表展示区")
public class HttpApiTreePanel extends AbstractListTreePanel {

    @Getter
    @Description("模块名, 模块结点")
    private final transient Map<String, ModuleNode> moduleNodeMap = new HashMap<>();

    @Getter
    @Description("模块名, controller")
    private final transient Map<String, List<PsiClass>> moduleControllerMap = new HashMap<>();

    @Getter
    @Description("controller, 方法列表")
    private final transient Map<PsiClass, List<MethodNode>> methodNodeMap = new HashMap<>();
    private final transient HttpServiceTool serviceTool;
    private final transient Project project;

    @Setter
    @Description("节点选中回调")
    private transient Consumer<MethodNode> chooseCallback;

    public HttpApiTreePanel(Project project) {
        super(new SimpleTree());
        this.project = project;
        this.serviceTool = HttpServiceTool.getInstance(project);
    }

    public ModuleNode initNodes(List<HttpEnum.HttpMethod> methods, List<String> nodeShowValues) {
        return initModuleNodes(methods, nodeShowValues);
    }

    @Description("初始化模块结点, 可能有多层级")
    private ModuleNode initModuleNodes(List<HttpEnum.HttpMethod> methods, List<String> nodeShowValues) {
        Collection<Module> modules = ProjectTool.moduleList(project);

        String contextPath;
        moduleNodeMap.remove(project.getName());
        // 初始化所有的模块结点
        for (Module m : modules) {
            contextPath = ProjectTool.getModuleContextPath(project, m);
            moduleNodeMap.put(m.getName(), new ModuleNode(new ModuleNodeData(m.getName(), contextPath)));
        }
        ModuleNode root = moduleNodeMap.get(project.getName());
        if (Objects.isNull(root)) {
            root = new ModuleNode(new ModuleNodeData(project.getName(), ""));
            moduleNodeMap.put(project.getName(), root);
        }

        if (methods.isEmpty()) {
            return root;
        }

        // 构建模块结点层级
        for (Module m : modules) {
            String moduleName = m.getName();
            VirtualFile[] contentRoots = ModuleRootManager.getInstance(m).getContentRoots();
            if (contentRoots.length < 1) {
                continue;
            }
            // 获取当前模块的父模块名称
            String parentName = contentRoots[0].getParent().getName();
            ModuleNode pn = moduleNodeMap.get(parentName);
            ModuleNode mn = moduleNodeMap.get(moduleName);
            if (!"Project".equals(parentName) && Objects.nonNull(pn)) {
                pn.add(mn);
            }

            List<PsiClass> controllers = ProjectTool.getModuleControllers(project, m);
            if (!controllers.isEmpty()) {
                moduleControllerMap.put(moduleName, controllers);
                isGenerateDefaultEnv(m);
                String controllerPath;
                for (PsiClass c : controllers) {
                    controllerPath = PsiTool.Annotation.getControllerPath(c);
                    contextPath = ProjectTool.getModuleContextPath(project, m);
                    methodNodeMap.put(c, buildMethodNodes(c, contextPath, controllerPath));
                }
                initPackageNodes(moduleName, methods, nodeShowValues).forEach(mn::add);
            }
        }

        removeEmptyModule();
        return root;
    }

    @Description("是否生成默认环境")
    private void isGenerateDefaultEnv(Module m) {
        String moduleName = m.getName();
        if (HttpSetting.getInstance().getGenerateDefault()) {
            String host = "127.0.0.1:" + ProjectTool.getModulePort(project, m);
            project.getMessageBus().syncPublisher(EnvListChangeTopic.TOPIC)
                    .save(moduleName, new HttpConfig(HttpEnum.Protocol.HTTP, host, Collections.emptyMap()));
        } else {
            project.getMessageBus().syncPublisher(EnvListChangeTopic.TOPIC).remove(moduleName);
        }
    }

    @Description("初始化包结点、类结点、方法结点")
    private List<BaseNode<? extends NodeData>> initPackageNodes(String moduleName, List<HttpEnum.HttpMethod> methods, List<String> nodeShowValues) {
        // 获取当前模块的所有 controller
        List<PsiClass> controllers = moduleControllerMap.get(moduleName);
        boolean isFilterPackage = nodeShowValues.contains(SETTING_VALUES.get(0));
        boolean isFilterClass = nodeShowValues.contains(SETTING_VALUES.get(1));

        if (Objects.isNull(controllers) || controllers.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, PackageNode> packageNodeMap = new HashMap<>(4);
        List<BaseNode<?>> children = new ArrayList<>();
        List<BaseNode<?>> unKnownPackage = new ArrayList<>(0);

        for (Map.Entry<PsiClass, List<MethodNode>> e : methodNodeMap.entrySet()) {
            PsiClass k = e.getKey();
            List<MethodNode> v = e.getValue();
            if (!controllers.contains(k)) {
                continue;
            }
            ClassNodeData data = new ClassNodeData(k);
            String s = PsiTool.Annotation.getSwaggerAnnotation(k, HttpEnum.AnnotationPlace.CLASS);
            data.setDescription(s);
            ClassNode classNode = new ClassNode(data);
            v.stream().filter(m -> methods.contains(m.getValue().getHttpMethod())).forEach(classNode::add);

            if (!isFilterPackage) {
                // 不显示包名则直接添加到 module 节点
                children.addAll(filterClass(classNode, isFilterClass));
            } else {
                String packageName = PsiTool.Class.getPackageName(k);
                if (classNode.getChildCount() > 0) {
                    if (Objects.isNull(packageName)) {
                        // 没有包名则直接添加到 module 节点
                        unKnownPackage.addAll(filterClass(classNode, isFilterClass));
                    } else {
                        PackageNode node = createPackageNodes(packageNodeMap, packageName);
                        filterClass(classNode, isFilterClass).forEach(node::add);
                    }
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

    private List<BaseNode<?>> filterClass(BaseNode<?> classNode, boolean isFilterClass) {
        List<BaseNode<?>> needToAddNode = new ArrayList<>();
        if (isFilterClass) {
            needToAddNode.add(classNode);
        } else {
            return TreeTool.findChildren(classNode);
        }
        return needToAddNode;
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

    @Override
    protected @Nullable JPopupMenu getRightClickMenu(@NotNull MouseEvent e, @NotNull BaseNode<?> node) {
        DefaultActionGroup group = new DefaultActionGroup();
        group.setPopup(true);
        if (!(node instanceof MethodNode mn)) {
            ExpandAction expandAction = new ExpandAction();
            expandAction.setAction(event -> treeExpand());
            group.add(expandAction);
        } else {
            CommonAction navigation = new CommonAction(Bundle.get("http.api.tree.method.right.menu.action.navigation"), "",
                    ThemeTool.isDark() ? HttpIcons.General.LOCATE : HttpIcons.General.LOCATE_LIGHT);
            navigation.setAction(event -> mn.getValue().getPsiElement().navigate(true));
            group.add(navigation);

            CopyAction copyFullPath = new CopyAction(Bundle.get("http.api.tree.method.right.menu.action.copy.full.path"));
            copyFullPath.setAction(event -> {
                HttpConfig config = serviceTool.getDefaultHttpConfig();
                SystemTool.copy2Clipboard(config.getProtocol().name().toLowerCase() + "://" + config.getHostValue() + mn.getFragment());
                NotifyService.instance(project).info(Bundle.get("http.api.tree.method.right.menu.action.copy.full.path.msg"));
            });
            group.add(copyFullPath);

            CopyAction copyApiPath = new CopyAction(Bundle.get("http.api.tree.method.right.menu.action.copy.api"));
            copyApiPath.setAction(event -> {
                SystemTool.copy2Clipboard(mn.getFragment());
                NotifyService.instance(project).info(Bundle.get("http.api.tree.method.right.menu.action.copy.api.msg"));
            });
            group.add(copyApiPath);
        }

        return ActionManager.getInstance().createActionPopupMenu("http", group).getComponent();
    }

    @Description("获取所有 @xxxMapping 的方法")
    public List<MethodNode> buildMethodNodes(@NotNull PsiClass psiClass, String contextPath, String controllerPath) {
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
                HttpEnum.HttpMethod httpMethod = httpMethodMap.get(qualifiedName);
                if (HttpEnum.HttpMethod.REQUEST.equals(httpMethod)) {
                    httpMethod = HttpEnum.HttpMethod.requestMappingConvert(annotation);
                }
                String name = PsiTool.Annotation.getAnnotationValue(annotation, new String[]{"value", "path"});
                MethodNodeData data1 = new MethodNodeData(httpMethod, name, controllerPath, contextPath);
                data1.setPsiElement(method);
                data = new MethodNode(data1);
                data1.setDescription(PsiTool.Annotation.getSwaggerAnnotation(method, HttpEnum.AnnotationPlace.METHOD));
                dataList.add(data);
            }
        }
        return dataList;
    }
}
