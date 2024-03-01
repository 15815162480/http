package com.zys.http.entity;

import com.zys.http.constant.HttpEnum;
import jdk.jfr.Description;
import lombok.Data;

import java.util.Map;

/**
 * @author zhou ys
 * @since 2024-03-01
 */
@Data
@Description("历史记录")
public class ReqHistory {
    private Integer id;
    private HttpEnum.HttpMethod method;
    private String host;
    private String uri;
    private Map<String, String> headers;
    private String contentType;
    private Map<String, String> params;
    private String body;
    private String[] fileNames;
    private String res;
    private String time;
}
