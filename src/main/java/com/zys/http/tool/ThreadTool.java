package com.zys.http.tool;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.*;

/**
 * @author zys
 * @since 2023-11-05
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThreadTool {
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            20,
            100,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy()
    );

    public static Future<?> submit(Runnable task) {
        return EXECUTOR.submit(task);
    }

    public static void execute(Runnable command) {
        EXECUTOR.execute(command);
    }

    public static ThreadPoolExecutor getExecutor() {
        return EXECUTOR;
    }
}
