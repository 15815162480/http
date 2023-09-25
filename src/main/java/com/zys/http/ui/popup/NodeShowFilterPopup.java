package com.zys.http.ui.popup;

import com.zys.http.service.Bundle;
import jdk.jfr.Description;

import java.util.List;

/**
 * @author zhou ys
 * @since 2023-09-25
 */
@Description("结点展示调车")
public class NodeShowFilterPopup extends AbstractFilterPopup<String> {

    public static final List<String> SETTING_VALUES = List.of(
            Bundle.get("http.filter.popup.node.show.package"),
            Bundle.get("http.filter.popup.node.show.class")
    );
    public NodeShowFilterPopup() {
        super(SETTING_VALUES);
    }
}
