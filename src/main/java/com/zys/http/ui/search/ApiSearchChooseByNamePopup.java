package com.zys.http.ui.search;

import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author zhou ys
 * @since 2023-10-10
 */
public class ApiSearchChooseByNamePopup extends ChooseByNamePopup {
    public static final Key<ApiSearchChooseByNamePopup> CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY = new Key<>("ApiSearchChoosePopup");
    protected ApiSearchChooseByNamePopup(@Nullable Project project, @NotNull ChooseByNameModel model, @NotNull ChooseByNameItemProvider provider, @Nullable ChooseByNamePopup oldPopup, @Nullable String predefinedText, boolean mayRequestOpenInCurrentWindow, int initialIndex) {
        super(project, model, provider, oldPopup, predefinedText, mayRequestOpenInCurrentWindow, initialIndex);
    }

    @NotNull
    public static ApiSearchChooseByNamePopup createPopup(final Project project,
                                                         @NotNull final ChooseByNameModel model,
                                                         @NotNull ChooseByNameItemProvider provider,
                                                         @Nullable final String predefinedText,
                                                         boolean mayRequestOpenInCurrentWindow,
                                                         final int initialIndex) {
        if (!StringUtil.isEmptyOrSpaces(predefinedText)) {
            return new ApiSearchChooseByNamePopup(project, model, provider, null, predefinedText, mayRequestOpenInCurrentWindow, initialIndex);
        }

        final ApiSearchChooseByNamePopup oldPopup = project == null ? null : project.getUserData(CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY);
        if (oldPopup != null) {
            oldPopup.close(false);
        }
        ApiSearchChooseByNamePopup newPopup = new ApiSearchChooseByNamePopup(project, model, provider, oldPopup, predefinedText, mayRequestOpenInCurrentWindow, initialIndex);

        if (project != null) {
            project.putUserData(CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY, newPopup);
        }
        return newPopup;
    }
}
