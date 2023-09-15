package com.zys.http.tool;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.*;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static cn.hutool.http.HttpUtil.createRequest;
import static com.zys.http.constant.HttpEnum.HttpMethod;

/**
 * @author zys
 * @since 2023-08-20
 */
@Description("发起请求客户端")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpClient {

    private static final int TIME_OUT = 10_000;

    private static final int REDIRECT_MAX_COUNT = 3;

    private static final ExecutorService EXECUTOR = ThreadUtil.newSingleExecutor();


    private static HttpRequest newRequest(
            @NotNull HttpMethod method,
            @NotNull String url,
            @NotNull Map<String, String> headers,
            String body
    ) {
        HttpRequest req = createRequest(Method.valueOf(method.name()), url).timeout(TIME_OUT);
        headers.forEach(req::header);

        if (Objects.isNull(body) || body.isBlank()) {
            return req;
        }

        req.body(body);

        if (body.contains("{") && body.contains("}") && JSONUtil.isTypeJSON(body)) {
            JSONObject json = JSONUtil.parseObj(body);
            for (Map.Entry<String, Object> entry : json.entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    continue;
                }
                url = url.replace("{" + entry.getKey() + "}", String.valueOf(value));
            }
            req.setUrl(url);
        }

        return req;
    }

    public static void run(
            @NotNull HttpRequest request,
            @Nullable Consumer<HttpResponse> onResult,
            @Nullable Consumer<Exception> onError,
            @Nullable Runnable onComplete
    ) {
        EXECUTOR.execute(() -> {
            try {
                HttpResponse response = request.execute();
                // 最大重定向的次数
                for (int i = 0; i < REDIRECT_MAX_COUNT && response.getStatus() == HttpStatus.HTTP_MOVED_TEMP; i++) {
                    String redirect = response.header(Header.LOCATION);
                    request.setUrl(redirect);
                    response = request.execute();
                }
                if (Objects.nonNull(onResult)) {
                    onResult.accept(response);
                }
            } catch (Exception e) {
                if (Objects.nonNull(onError)) {
                    onError.accept(e);
                    return;
                }
                throw e;
            } finally {
                if (Objects.nonNull(onComplete)) {
                    onComplete.run();
                }
            }
        });
    }
}
