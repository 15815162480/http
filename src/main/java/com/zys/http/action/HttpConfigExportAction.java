package com.zys.http.action;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.zys.http.constant.HttpEnum;
import com.zys.http.entity.HttpConfig;
import com.zys.http.service.Bundle;
import com.zys.http.service.NotifyService;
import com.zys.http.tool.HttpServiceTool;
import com.zys.http.tool.VelocityTool;
import com.zys.http.ui.tree.node.MethodNode;
import jdk.jfr.Description;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhou ys
 * @since 2023-09-26
 */
@Description("配置导出通用操作")
public class HttpConfigExportAction extends ExportAction {
    private final HttpEnum.ExportEnum exportEnum;

    public HttpConfigExportAction(String text, @NotNull HttpEnum.ExportEnum exportEnum) {
        super(text);
        this.exportEnum = exportEnum;
    }

    /**
     * 根据枚举类型对不同数据进行导出
     *
     * @param moduleControllerMap 树形结构中的 module controllers Map
     * @param methodNodeMap       树形结构中的 class methods Map
     * @param selectedEnv         选中的环境
     */
    public void initAction(
            @Nullable Map<String, List<PsiClass>> moduleControllerMap,
            @Nullable Map<PsiClass, List<MethodNode>> methodNodeMap,
            @Nullable String selectedEnv
    ) {

        this.setAction(e -> {
            Project project = e.getProject();
            if (null == project) {
                return;
            }
            VirtualFile selectedFile = null;
            try {
                HttpServiceTool serviceTool = HttpServiceTool.getInstance(project);
                HttpConfig config = serviceTool.getDefaultHttpConfig();
                selectedFile = createFileChooser(project);
                String path = selectedFile.getPath();
                String exportEnv = Objects.isNull(selectedEnv)? serviceTool.getSelectedEnv(): selectedEnv;
                switch (exportEnum) {
                    case API -> {
                        if (moduleControllerMap != null) {
                            VelocityTool.exportAllModuleApi(moduleControllerMap, methodNodeMap, path);
                        }
                    }
                    case ALL_ENV -> VelocityTool.exportAllEnv(serviceTool.getHttpConfigs(), path);
                    case SPECIFY_ENV -> VelocityTool.exportEnv(exportEnv, config, path);
                    default -> {
                        // 不处理
                    }
                }
                NotifyService.instance(project).info(Bundle.get("http.message.export.success"));
            } catch (IOException ex) {
                if (Objects.nonNull(selectedFile)) {
                    NotifyService.instance(project).error(Bundle.get("http.message.export.fail"));
                }
            }
        });

    }


    @Description("创建文件选择对话框")
    private VirtualFile createFileChooser(Project project) throws IOException {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        descriptor.setTitle(Bundle.get("http.dialog.env.export"));
        FileChooserFactory.getInstance().createFileChooser(descriptor, project, null);
        VirtualFile selectedFile = FileChooser.chooseFile(descriptor, project, null);
        if (Objects.isNull(selectedFile)) {
            NotifyService.instance(project).error("http.message.export.unselect.folder");
            throw new IOException("A");
        }
        return selectedFile;
    }
}
