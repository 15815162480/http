package com.zys.http.ui.popup;

import com.zys.http.constant.HttpEnum;
import jdk.jfr.Description;

/**
 * @author zhou ys
 * @since 2023-09-20
 */
@Description("请求方法过滤菜单")
public class MethodFilterPopup extends AbstractFilterPopup<HttpEnum.HttpMethod> {
    public MethodFilterPopup(HttpEnum.HttpMethod[] values) {
        super(values);
    }
}
