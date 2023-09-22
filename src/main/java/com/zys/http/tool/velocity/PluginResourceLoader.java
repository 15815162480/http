package com.zys.http.tool.velocity;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.InputStream;

/**
 * @author zhou ys
 * @since 2023-09-22
 */
public class PluginResourceLoader extends ClasspathResourceLoader {
    @Override
    public InputStream getResourceStream(String name) throws ResourceNotFoundException {
        return super.getResourceStream("template/" + name);
    }
}