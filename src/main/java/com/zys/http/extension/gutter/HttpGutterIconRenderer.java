package com.zys.http.extension.gutter;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.psi.PsiIdentifier;
import com.zys.http.action.CommonAction;
import com.zys.http.extension.service.Bundle;
import com.zys.http.ui.icon.HttpIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author zhou ys
 * @since 2023-10-12
 */
public class HttpGutterIconRenderer extends LineMarkerInfo.LineMarkerGutterIconRenderer<PsiIdentifier> {
    public final CommonAction generateRequestAction;

    public HttpGutterIconRenderer(@NotNull LineMarkerInfo<PsiIdentifier> info, CommonAction generateRequestAction) {
        super(info);
        this.generateRequestAction = generateRequestAction;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public @NotNull Icon getIcon() {
        return HttpIcons.HttpMethod.REQUEST;
    }

    @Override
    public @Nullable AnAction getClickAction() {
        return generateRequestAction;
    }

    @Override
    public String getTooltipText() {
        return Bundle.get("http.gutter.config");
    }
}
