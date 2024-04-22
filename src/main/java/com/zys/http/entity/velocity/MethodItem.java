package com.zys.http.entity.velocity;

import jdk.jfr.Description;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author zys
 * @since 2023-09-23
 */
@Data
@Description("Velocity 模板渲染请求方式节点数据模型")
public class MethodItem {
    @Description("请求名字")
    private String name;

    @Description("请求方式")
    private String method;

    @Description("请求类型")
    private String contentType;

    @Description("请求 uri")
    private String uri;

    @Description("请求体类型, raw、urlencoded")
    private String mode;

    @Description("请求体类型为 raw 时自动转换默认 json")
    private String raw;

    @Description("请求 url 参数 Key")
    private List<String> queryKey;

    @Description("请求 url urlencoded 参数 Key")
    private Set<String> urlencodedKey;
}
