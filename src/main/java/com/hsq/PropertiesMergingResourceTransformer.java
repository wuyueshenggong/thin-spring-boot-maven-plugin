//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import org.apache.maven.plugins.shade.relocation.Relocator;
import org.apache.maven.plugins.shade.resource.ResourceTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class PropertiesMergingResourceTransformer implements ResourceTransformer {
    private String resource;
    private final Properties data = new Properties();

    public PropertiesMergingResourceTransformer() {
    }

    public Properties getData() {
        return this.data;
    }
    @Override
    public boolean canTransformResource(String resource) {
        return this.resource != null && this.resource.equalsIgnoreCase(resource);
    }
    @Override
    public void processResource(String resource, InputStream inputStream, List<Relocator> relocators) throws IOException {
        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();
        properties.forEach((name, value) -> {
            this.process((String)name, (String)value);
        });
    }

    private void process(String name, String value) {
        String existing = this.data.getProperty(name);
        this.data.setProperty(name, existing != null ? existing + "," + value : value);
    }
    @Override
    public boolean hasTransformedResource() {
        return !this.data.isEmpty();
    }
    @Override
    public void modifyOutputStream(JarOutputStream os) throws IOException {
        os.putNextEntry(new JarEntry(this.resource));
        this.data.store(os, "Merged by PropertiesMergingResourceTransformer");
        os.flush();
        this.data.clear();
    }

    public String getResource() {
        return this.resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
