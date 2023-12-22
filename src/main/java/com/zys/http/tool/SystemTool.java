package com.zys.http.tool;

import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.Objects;

/**
 * @author zys
 * @since 2023-09-19
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemTool {

    @Description("将文本复制到剪切版")
    public static void copy2Clipboard(String text) {
        // 获取系统剪贴板
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        // 封装文本内容
        Transferable trans = new StringSelection(text);
        // 把文本内容设置到系统剪贴板
        clipboard.setContents(trans, null);
    }

    @Description("获取剪切版文本")
    public static String getClipboardContent() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clipboard.getContents(null);

        if (Objects.nonNull(transferable) && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                return (String) transferable.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException e) {
                return null;
            }
        }

        return null;
    }
}
