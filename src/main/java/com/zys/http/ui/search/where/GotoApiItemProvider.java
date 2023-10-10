package com.zys.http.ui.search.where;

import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNameViewModel;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author zys
 * @since 2023-10-10
 */
public class GotoApiItemProvider implements ChooseByNameItemProvider {
    private final GotoApiModel model;

    public GotoApiItemProvider(GotoApiModel model){
        this.model = model;
    }
    @Override
    public @NotNull List<String> filterNames(@NotNull ChooseByNameViewModel base, String @NotNull [] names, @NotNull String pattern) {
        return Collections.emptyList();
    }

    @Override
    public boolean filterElements(@NotNull ChooseByNameViewModel base, @NotNull String pattern, boolean everywhere, @NotNull ProgressIndicator cancelled, @NotNull Processor<Object> consumer) {
        return false;
    }
}
