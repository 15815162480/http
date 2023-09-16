package com.zys.http.entity;

import jdk.jfr.Description;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.zys.http.constant.HttpEnum.Protocol;

/**
 * @author zys
 * @since 2023-08-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Description("选择的配置数据")
public class HttpConfig {

    @Description("选中的协议")
    private Protocol protocol;

    @Description("设置的IP/HOST")
    private String hostValue;

    @Description("请求头")
    private Map<String, Object> headers;
}
