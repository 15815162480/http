package com.zys.http.entity.param;

import com.zys.http.constant.HttpEnum;
import jdk.jfr.Description;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zys
 * @since 2023-09-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Description("请求参数")
public class ParamProperty {

    /**
     * 如果请求参数没有 @RequestBody 注解的情况下, 且不是基础数据类型的情况下<br>
     * 应该读取该类的所有字段分别处理成各个基础数据类型, 即该值只能是基础数据类型<br>
     * 如果是 Body 类型, 应为 json 格式字符串, 只处理两层
     */
    private Object defaultValue;

    private HttpEnum.ParamUsage paramUsage;

    @Override
    public String toString() {
        return defaultValue.toString();
    }
}
