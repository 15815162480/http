package com.zys.http.constant;

import jdk.jfr.Description;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author zhou ys
 * @since 2023-10-11
 */
@Description("插件级别常量")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpConstant {
    public static final String PLUGIN_NAME = "ApiTool";

    public static final String PLUGIN_CONFIG_FILE_NAME = "httpService.xml";

    public static final String EDIT_AS_PROPERTIES_TEMPLATE = "{}-{}";
}
