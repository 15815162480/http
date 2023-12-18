package com.zys.http.tool.ui;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.impl.FileTypeRenderer;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.SimpleListCellRenderer;
import com.zys.http.constant.HttpEnum;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author zhou ys
 * @since 2023-12-18
 */
@Description("复选框工具类")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ComboBoxTool {

    public static ComboBox<FileType> fileTypeComboBox(List<FileType> fileTypes, @Nullable ItemListener listener) {
        return createComboBox(fileTypes, new FileTypeRenderer(), listener);
    }

    public static ComboBox<HttpEnum.Protocol> protocolComboBox() {
        return createComboBox(Arrays.stream(HttpEnum.Protocol.values()).toList(), null, null);
    }

    @SuppressWarnings("unchecked")
    private static <T> ComboBox<T> createComboBox(@NotNull List<T> items, @Nullable SimpleListCellRenderer<? super T> renderer, @Nullable ItemListener listener) {
        ComboBox<T> box = new ComboBox<>((T[]) items.toArray());
        box.setFocusable(false);
        if (Objects.nonNull(renderer)) {
            box.setRenderer(renderer);
        }
        if (Objects.nonNull(listener)) {
            box.addItemListener(listener);
        }
        return box;
    }
}
