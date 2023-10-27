package com.zys.http.tool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.MultiFileResource;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.*;
import com.intellij.openapi.fileTypes.FileType;
import com.zys.http.tool.convert.ParamConvert;
import com.zys.http.ui.editor.CustomEditor;
import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.regex.Pattern;

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
    private static final Pattern JSON_PATTERN = Pattern.compile("application/json", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_PATTERN = Pattern.compile("text/html", Pattern.CASE_INSENSITIVE);
    private static final Pattern XML_PATTERN = Pattern.compile("text/xml", Pattern.CASE_INSENSITIVE);


    public static HttpRequest newRequest(
            @NotNull HttpMethod method,
            @NotNull String url,
            @NotNull Map<String, String> headers,
            Map<String, String> parameters,
            String body,
            String partName,
            String[] fileNames
    ) {
        HttpRequest req = HttpUtil.createRequest(Method.valueOf(method.name()), url).timeout(TIME_OUT);
        headers.forEach((name, value) -> req.header(name, String.valueOf(value)));

        if (Objects.nonNull(body) && !body.isBlank()) {
            req.body(body);
        }

        if (!parameters.isEmpty()) {
            String s = ParamConvert.buildUrlParameters(parameters);
            url = url.endsWith("/") ? url.substring(0, url.lastIndexOf('/')) : url;
            req.setUrl(url + "?" + s);
        }

        if (Objects.nonNull(fileNames) && fileNames.length > 0) {
            MultiFileResource resources = new MultiFileResource(Arrays.stream(fileNames).map(FileUtil::file).toList());
            req.form(partName, resources);
        }

        return req;
    }

    public static void run(
            @NotNull HttpRequest request,
            @Nullable Consumer<HttpResponse> onResult,
            @Nullable Consumer<Exception> onError,
            @Nullable Consumer<Long> onComplete
    ) {
        EXECUTOR.execute(() -> {
            long startTime = System.currentTimeMillis();
            long endTime = 0;
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
                endTime = System.currentTimeMillis();
            } catch (Exception e) {
                if (Objects.nonNull(onError)) {
                    onError.accept(e);
                    return;
                }
                endTime = System.currentTimeMillis();
                throw e;
            } finally {
                if (Objects.nonNull(onComplete)) {
                    onComplete.accept(endTime - startTime);
                }
            }
        });
    }

    @NotNull
    public static FileType parseFileType(@NotNull HttpResponse response) {
        FileType fileType = CustomEditor.TEXT_FILE_TYPE;
        // Content-Type
        final String contentType = response.header(Header.CONTENT_TYPE);

        if (contentType != null) {
            if (JSON_PATTERN.matcher(contentType).find()) {
                fileType = CustomEditor.JSON_FILE_TYPE;
            } else if (HTML_PATTERN.matcher(contentType).find()) {
                fileType = CustomEditor.HTML_FILE_TYPE;
            } else if (XML_PATTERN.matcher(contentType).find()) {
                fileType = CustomEditor.XML_FILE_TYPE;
            }
        }
        return fileType;
    }
}
