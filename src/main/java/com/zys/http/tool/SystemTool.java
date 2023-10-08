package com.zys.http.tool;

import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.Timer;
import java.util.TimerTask;

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

    @Description("延时任务")
    public static void schedule(Entrust entrust, int delay) {
        Timer timer = new Timer();
        TimerTask task1 = new TimerTask() {
            @Override
            public void run() {
                entrust.run();
            }
        };
        timer.schedule(task1, delay);
    }
}
