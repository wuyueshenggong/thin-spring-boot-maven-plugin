//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.artifact.filter.collection.AbstractArtifactFeatureFilter;

public class MatchingGroupIdFilter extends AbstractArtifactFeatureFilter {
    public MatchingGroupIdFilter(String exclude) {
        super("", exclude);
    }
    @Override
    protected String getArtifactFeature(Artifact artifact) {
        return artifact.getGroupId();
    }
}
