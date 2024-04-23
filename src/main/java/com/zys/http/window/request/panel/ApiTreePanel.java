package com.zys.http.window.request.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ReadAction;
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
import com.zys.http.entity.tree.ClassNodeData;
import com.zys.http.entity.tree.MethodNodeData;
import com.zys.http.entity.tree.ModuleNodeData;
import com.zys.http.entity.tree.PackageNodeData;
import com.zys.http.extension.service.Bundle;
import com.zys.http.extension.service.NotifyService;
import com.zys.http.extension.setting.HttpSetting;
import com.zys.http.extension.topic.EnvironmentTopic;
import com.zys.http.tool.*;
import com.zys.http.tool.ui.TreeTool;
import com.zys.http.ui.tree.AbstractListTreePanel;
import com.zys.http.ui.tree.node.*;
import jdk.jfr.Description;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zys.http.ui.popup.NodeShowFilterPopup.SETTING_VALUES;

/**
 * @author zhou ys
 * @since 2024-04-16
 */
@Description("API 标签页面-API 树形展示组件")
final class ApiTreePanel extends AbstractListTreePanel {
    private transient ModuleNode root;
    private final transient Project project;

    @Description("模块名, 模块结点")
    private final transient Map<String, ModuleNode> moduleNodeMap = new HashMap<>();

    @Description("模块名, controller")
    private final transient Map<String, List<PsiClass>> moduleControllerMap = new HashMap<>();

    @Description("controller, 方法列表")
    private final transient Map<PsiClass, List<MethodNode>> methodNodeMap = new HashMap<>();

    @Setter
    @Description("节点选中回调")
    private transient Consumer<MethodNode> chooseCallback;

    public ApiTreePanel(Project project) {
        super(new SimpleTree());
        this.project = project;
    }

    public void loadNodes(List<HttpEnum.HttpMethod> methods, List<String> nodeShowValues) {
        loadModuleNodes(methods, nodeShowValues);
        getTreeModel().setRoot(root);
    }

    private void loadModuleNodes(@NotNull List<HttpEnum.HttpMethod> methods, List<String> nodeShowValues) {
        Collection<Module> modules = ProjectTool.moduleList(project);
        String projectName = project.getName();

        moduleNodeMap.remove(projectName);
        Map<String, String> moduleContextPathMap = modules.stream().collect(Collectors.toMap(Module::getName, module -> ProjectTool.getModuleContextPath(project, module)));

        modules.forEach(module -> {
            String moduleName = module.getName();
            String contextPath = moduleContextPathMap.get(moduleName);
            ModuleNode moduleNode = new ModuleNode(new ModuleNodeData(moduleName, contextPath));
            moduleNodeMap.put(moduleName, moduleNode);
        });
        root = Optional.ofNullable(moduleNodeMap.get(projectName)).orElseGet(() -> {
            ModuleNode moduleNode = new ModuleNode(new ModuleNodeData(project.getName(), ""));
            moduleNodeMap.put(projectName, moduleNode);
            return moduleNode;
        });

        if (methods.isEmpty()) {
            getTreeModel().setRoot(root);
            return;
        }

        Map<String, VirtualFile[]> moduleContentRootsMap = modules.stream().collect(Collectors.toMap(Module::getName, module -> ModuleRootManager.getInstance(module).getContentRoots()));
        for (Module module : modules) {
            String moduleName = module.getName();
            VirtualFile[] contentRoots = moduleContentRootsMap.getOrDefault(moduleName, new VirtualFile[0]);
            if (contentRoots.length == 0) {
                continue;
            }
            String parentName = contentRoots[0].getParent().getName();
            ModuleNode parentNode = moduleNodeMap.get(parentName);
            ModuleNode moduleNode = moduleNodeMap.get(moduleName);
            if (Objects.nonNull(parentNode)) {
                parentNode.add(moduleNode);
            }
            String contextPath = moduleContextPathMap.get(moduleName);
            loadClassNodes(module, contextPath, methods, nodeShowValues);
        }
        removeEmptyModule();
    }

    private void loadClassNodes(Module module, String contextPath, List<HttpEnum.HttpMethod> methods, List<String> nodeShowValues) {
        List<PsiClass> controllers = ProjectTool.getModuleControllers(project, module);
        if (controllers.isEmpty()) {
            return;
        }
        moduleControllerMap.put(module.getName(), controllers);
        isGenerateDefaultEnv(module);
        controllers.forEach(controller -> loadMethodNodes(controller, contextPath));
        loadPackageNodes(module, methods, nodeShowValues);
    }

    @SneakyThrows
    private void loadMethodNodes(@NotNull PsiClass psiClass, String contextPath) {
        if (psiClass.isAnnotationType() || psiClass.isInterface() || psiClass.isEnum()) {
            return;
        }

        List<MethodNode> methodNodes = ReadAction.nonBlocking(() -> {
            PsiMethod[] methods = psiClass.getAllMethods();
            String controllerPath = PsiTool.Annotation.getControllerPath(psiClass);
            return Arrays.stream(methods)
                    .map(method -> createMethodNodes(method, controllerPath, contextPath)).flatMap(List::stream).toList();
        }).submit(ThreadTool.getExecutor()).get();

        methodNodeMap.put(psiClass, methodNodes);
    }

    private void loadPackageNodes(@NotNull Module module, List<HttpEnum.HttpMethod> methods, @NotNull List<String> nodeShowValues) {
        // 获取当前模块的所有 controller
        String moduleName = module.getName();
        List<PsiClass> controllers = moduleControllerMap.get(moduleName);
        boolean isFilterPackage = nodeShowValues.contains(SETTING_VALUES.get(0));
        boolean isFilterClass = nodeShowValues.contains(SETTING_VALUES.get(1));

        if (Objects.isNull(controllers) || controllers.isEmpty()) {
            return;
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
                String packageName = PsiTool.Class.packageName(k);
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

        ModuleNode moduleNode = moduleNodeMap.get(moduleName);
        children.forEach(moduleNode::add);
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

    private @NotNull List<BaseNode<?>> filterClass(BaseNode<?> classNode, boolean isFilterClass) {
        List<BaseNode<?>> needToAddNode = new ArrayList<>();
        if (isFilterClass) {
            needToAddNode.add(classNode);
        } else {
            return TreeTool.findChildren(classNode);
        }
        return needToAddNode;
    }

    private void isGenerateDefaultEnv(@NotNull Module m) {
        String moduleName = m.getName();
        if (HttpSetting.getInstance().getGenerateDefault()) {
            String host = "127.0.0.1:" + ProjectTool.getModulePort(project, m);
            project.getMessageBus().syncPublisher(EnvironmentTopic.LIST_TOPIC)
                    .save(moduleName, new HttpConfig(HttpEnum.Protocol.HTTP, host, Collections.emptyMap()));
        } else {
            project.getMessageBus().syncPublisher(EnvironmentTopic.LIST_TOPIC).remove(moduleName);
        }
    }

    private List<MethodNode> createMethodNodes(@NotNull PsiMethod method, String controllerPath, String contextPath) {
        PsiAnnotation[] annotations = method.getAnnotations();
        return Stream.of(annotations).filter(SpringEnum.Method::contains)
                .map(annotation -> {
                    HttpEnum.HttpMethod httpMethod = SpringEnum.Method.get(annotation);
                    String name = PsiTool.Annotation.getAnnotationValue(annotation, new String[]{"value", "path"});
                    MethodNodeData data = new MethodNodeData(httpMethod, name, controllerPath, contextPath);
                    data.setDescription(PsiTool.Annotation.getSwaggerAnnotation(method, HttpEnum.AnnotationPlace.METHOD));
                    data.setPsiElement(method);
                    return new MethodNode(data);
                }).toList();
    }

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

    public MethodNode getMethodNode(@NotNull PsiMethod psiMethod) {
        PsiClass containingClass = psiMethod.getContainingClass();
        return methodNodeMap.getOrDefault(containingClass, new ArrayList<>()).stream()
                .filter(v -> v.getValue().getPsiElement().equals(psiMethod)).findFirst().orElse(null);
    }

    @Override
    @Contract(pure = true)
    protected @NotNull Consumer<BaseNode<?>> getChooseListener() {
        return node -> {
            if (node instanceof MethodNode methodNode && Objects.nonNull(chooseCallback)) {
                chooseCallback.accept(methodNode);
            }
        };
    }

    @Override
    @Contract(pure = true)
    protected @NotNull Consumer<BaseNode<?>> getDoubleClickListener() {
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
    protected @NotNull JPopupMenu getRightClickMenu(@NotNull MouseEvent e, @NotNull BaseNode<?> node) {
        DefaultActionGroup group = new DefaultActionGroup();
        HttpServiceTool serviceTool = HttpServiceTool.getInstance(project);
        group.setPopup(true);
        if (!(node instanceof MethodNode mn)) {
            ExpandAction expandAction = new ExpandAction();
            expandAction.setAction(event -> treeExpand());
            group.add(expandAction);
        } else {
            CommonAction navigation = new CommonAction(Bundle.get("http.api.tree.method.right.menu.action.navigation"), "", AllIcons.General.Locate);
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

    @Override
    public void clear() {
        root = null;
        this.getTreeModel().setRoot(null);
    }
}
