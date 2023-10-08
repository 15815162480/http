package com.zys.http.tool;

import jdk.jfr.Description;

/**
 * @author zhou ys
 * @since 2023-10-08
 */
@FunctionalInterface
@Description("委托者, 委托这个接口去做的事")
public interface Entrust {
    void run();
}
