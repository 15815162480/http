package com.zys.http.ui.tree;

import cn.hutool.core.text.CharSequenceUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.treeStructure.SimpleTree;
import com.zys.http.entity.tree.ClassNodeData;
import com.zys.http.entity.tree.NodeData;
import com.zys.http.entity.tree.PackageNodeData;
import com.zys.http.tool.PsiTool;
import com.zys.http.ui.tree.node.*;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.*;

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
        super.getTreeModel().setRoot(PsiTool.buildHttpApiTreeNodeData(project));
        initTreeNode();
    }

    @Override
    public void collapseAll() {

    }

    private void initTreeNode() {
        List<DefaultMutableTreeNode> allLeafNodes = getAllLeafNodes();
        for (DefaultMutableTreeNode node : allLeafNodes) {
            String nodeName = ((BaseNode<? extends NodeData>) node).getValue().getNodeName();
            List<PsiClass> controller = PsiTool.getModuleController(project, PsiTool.getModuleByName(project, nodeName));
            if (!controller.isEmpty()) {
                String classCommonPackagePrefix = getClassCommonPackagePrefix(controller);
                PackageNode commonPackageNode = null;
                if (CharSequenceUtil.isNotEmpty(classCommonPackagePrefix)) {
                    commonPackageNode = new PackageNode(new PackageNodeData(classCommonPackagePrefix));
                    node.add(commonPackageNode);
                }
                initPackageAndClassNode(node, controller, classCommonPackagePrefix, commonPackageNode);
            } else {
                TreeNode parent = node.getParent();
                if (!node.isRoot()) {
                    getTreeModel().removeNodeFromParent(node);
                }
                if (parent != null && parent.isLeaf()) {
                    getTreeModel().removeNodeFromParent((MutableTreeNode) parent);
                }
            }
        }
    }

    private void initPackageAndClassNode(DefaultMutableTreeNode currentNode, List<PsiClass> controller, String classCommonPackagePrefix, PackageNode commonPackageNode) {
        String contextPath = ((ModuleNode) currentNode).getValue().getContextPath();
        for (PsiClass psiClass : controller) {
            String qualifiedName = psiClass.getQualifiedName();
            if (qualifiedName != null && classCommonPackagePrefix != null) {
                // 先把当前类名移除掉
                qualifiedName = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
                if (qualifiedName.equals(classCommonPackagePrefix)) {
                    // 如果相同直接添加当前类
                    addChildPackageNode(commonPackageNode, currentNode, psiClass);
                } else {
                    if (Objects.nonNull(commonPackageNode)) {
                        addChildPackageNodeWhenHasSubPackage(qualifiedName, classCommonPackagePrefix, psiClass, commonPackageNode, contextPath);
                    } else {
                        currentNode.add(new ClassNode(new ClassNodeData(psiClass, contextPath)));
                    }
                }
            } else {
                currentNode.add(new ClassNode(new ClassNodeData(psiClass, contextPath)));
            }
        }
    }

    @Description("添加子结点, 没有子包时")
    private void addChildPackageNode(PackageNode commonPackageNode, DefaultMutableTreeNode currentNode, PsiClass psiClass) {
        String contextPath = ((ModuleNode) currentNode).getValue().getContextPath();
        if (Objects.nonNull(commonPackageNode)) {
            commonPackageNode.add(new ClassNode(new ClassNodeData(psiClass, contextPath)));
        } else {
            currentNode.add(new ClassNode(new ClassNodeData(psiClass, contextPath)));
        }
    }

    @Description("添加包结点, 当有子包时")
    private void addChildPackageNodeWhenHasSubPackage(@NotNull String qualifiedName, @NotNull String classCommonPackagePrefix, PsiClass psiClass, PackageNode commonPackageNode, String contextPath) {
        qualifiedName = qualifiedName.substring(classCommonPackagePrefix.length() + 1);
        DefaultMutableTreeNode treeNode = findNodeByContent(commonPackageNode, qualifiedName);
        if (Objects.isNull(treeNode)) {
            PackageNode restPackageNode = new PackageNode(new PackageNodeData(qualifiedName));
            restPackageNode.add(new ClassNode(new ClassNodeData(psiClass, contextPath)));
            commonPackageNode.add(restPackageNode);
        } else {
            treeNode.add(new ClassNode(new ClassNodeData(psiClass, contextPath)));
        }
    }


    @Description("获取类的公共包名")
    private String getClassCommonPackagePrefix(List<PsiClass> psiClassList) {
        if (psiClassList.isEmpty()) {
            return null;
        }
        List<String> packageNames = psiClassList.stream().map(PsiClass::getQualifiedName).filter(Objects::nonNull)
                .map(v -> v.substring(0, v.lastIndexOf('.'))).toList();

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
        addLeafNodes(root, leafNodes);
        return leafNodes;
    }

    private void addLeafNodes(DefaultMutableTreeNode node, List<DefaultMutableTreeNode> leafNodes) {
        if (node.isLeaf()) {
            leafNodes.add(node);
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                addLeafNodes(childNode, leafNodes);
            }
        }
    }
}
