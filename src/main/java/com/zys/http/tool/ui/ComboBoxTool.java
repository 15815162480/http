package com.zys.http.tool.ui;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypes;
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
import java.util.ArrayList;
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

    public static final FileType TEXT_FILE_TYPE = FileTypes.PLAIN_TEXT;

    public static final FileType JSON_FILE_TYPE = JsonFileType.INSTANCE;

    public static final FileType HTML_FILE_TYPE = HtmlFileType.INSTANCE;

    public static final FileType XML_FILE_TYPE = XmlFileType.INSTANCE;

    private static final List<FileType> FILE_TYPE_LIST = new ArrayList<>();

    static {
        FILE_TYPE_LIST.add(TEXT_FILE_TYPE);
        FILE_TYPE_LIST.add(JSON_FILE_TYPE);
        FILE_TYPE_LIST.add(HTML_FILE_TYPE);
        FILE_TYPE_LIST.add(XML_FILE_TYPE);
    }

    public static ComboBox<FileType> fileTypeComboBox(@Nullable ItemListener listener) {
        return createComboBox(FILE_TYPE_LIST, new FileTypeRenderer(), listener);
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
