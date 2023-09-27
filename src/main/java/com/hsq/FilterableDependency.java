//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import org.apache.maven.plugins.annotations.Parameter;

abstract class FilterableDependency {
    @Parameter(
        required = true
    )
    private String groupId;
    @Parameter(
        required = true
    )
    private String artifactId;
    @Parameter
    private String classifier;

    FilterableDependency() {
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return this.artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getClassifier() {
        return this.classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }
}
