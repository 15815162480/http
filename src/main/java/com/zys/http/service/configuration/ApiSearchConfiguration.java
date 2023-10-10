package com.zys.http.service.configuration;

import com.intellij.ide.util.gotoByName.ChooseByNameFilterConfiguration;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.zys.http.constant.HttpEnum;
import org.jetbrains.annotations.NotNull;

/**
 * @author zhou ys
 * @since 2023-10-10
 */
@State(name = "ApiSearchConfiguration", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class ApiSearchConfiguration extends ChooseByNameFilterConfiguration<HttpEnum.HttpMethod> {
    public static ApiSearchConfiguration getInstance(@NotNull Project project) {
        return project.getService(ApiSearchConfiguration.class);
    }

    @Override
    protected String nameForElement(@NotNull HttpEnum.HttpMethod type) {
        return type.name();
    }
}
