package com.zys.http.extension.search.everywhere;

import com.intellij.ide.actions.searcheverywhere.AbstractGotoSEContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.zys.http.constant.HttpConstant;
import com.zys.http.constant.HttpEnum;
import com.zys.http.constant.SpringEnum;
import com.zys.http.entity.tree.MethodNodeData;
import com.zys.http.extension.setting.HttpSetting;
import com.zys.http.tool.ProjectTool;
import com.zys.http.tool.PsiTool;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zys
 * @since 2023-10-10
 */
public class GotoApiSearchEverywhereContributor extends AbstractGotoSEContributor {
    private final GotoApiModel apiModel;

    public GotoApiSearchEverywhereContributor(@NotNull AnActionEvent event) {
        super(event);
        Project project = event.getProject();
        this.apiModel = new GotoApiModel(project, new GotoApiChooseByNameContributor(methodNodeDataList(project)));
    }

    private static List<MethodNodeData> methodNodeDataList(Project project) {
        Collection<Module> moduleList = ProjectTool.moduleList(project);
        List<MethodNodeData> methodNodeDataList = new ArrayList<>();
        Map<String, HttpEnum.HttpMethod> httpMethodMap = Arrays.stream(SpringEnum.Method.values())
                .collect(Collectors.toMap(SpringEnum.Method::getClazz, SpringEnum.Method::getHttpMethod));
        for (Module m : moduleList) {
            List<PsiClass> controllers = ProjectTool.getModuleControllers(project, m).stream()
                    .filter(c -> c.getAllMethods().length > 0)
                    .filter(c -> !PsiTool.Class.getAllXxxMappingMethods(c).isEmpty())
                    .toList();
            for (PsiClass c : controllers) {
                List<PsiMethod> xxxMappingMethods = PsiTool.Class.getAllXxxMappingMethods(c);
                String controllerPath = PsiTool.Annotation.getControllerPath(c);
                String contextPath = ProjectTool.getModuleContextPath(project, m);
                for (PsiMethod method : xxxMappingMethods) {
                    PsiAnnotation[] annotations = method.getAnnotations();
                    for (PsiAnnotation annotation : annotations) {
                        String qualifiedName = annotation.getQualifiedName();
                        if (httpMethodMap.containsKey(qualifiedName)) {
                            HttpEnum.HttpMethod httpMethod = httpMethodMap.get(qualifiedName);
                            if (HttpEnum.HttpMethod.REQUEST.equals(httpMethod)) {
                                httpMethod = HttpEnum.HttpMethod.requestMappingConvert(annotation);
                            }
                            String name = PsiTool.Annotation.getAnnotationValue(annotation, new String[]{"value", "path"});
                            MethodNodeData data = new MethodNodeData(httpMethod, name, controllerPath, contextPath);
                            data.setPsiElement(method);
                            methodNodeDataList.add(data);
                        }
                    }
                }
            }
        }

        return methodNodeDataList;
    }

    @Override
    public boolean isShownInSeparateTab() {
        return HttpSetting.getInstance().getEnableSearchEverywhere() && !apiModel.getNodeDataList().isEmpty();
    }

    @Override
    public @NotNull String getSearchProviderId() {
        return GotoApiSearchEverywhereContributor.class.getSimpleName();
    }

    @Override
    public @NotNull @Nls String getGroupName() {
        return HttpConstant.PLUGIN_NAME;
    }

    @Override
    public int getSortWeight() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected @NotNull FilteringGotoByModel<?> createModel(@NotNull Project project) {
        return apiModel;
    }

    public static class Factory implements SearchEverywhereContributorFactory<Object> {
        @Override
        public @NotNull SearchEverywhereContributor<Object> createContributor(@NotNull AnActionEvent initEvent) {
            return new GotoApiSearchEverywhereContributor(initEvent);
        }
    }
}
